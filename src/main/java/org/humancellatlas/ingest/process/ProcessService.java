package org.humancellatlas.ingest.process;

import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

@Service
@Getter
public class ProcessService {

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private BiomaterialRepository biomaterialRepository;
    @Autowired
    private BundleManifestRepository bundleManifestRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private MetadataCrudService metadataCrudService;
    @Autowired
    private MetadataUpdateService metadataUpdateService;


    @Autowired
    MetadataDocumentEventHandler metadataDocumentEventHandler;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Page<Biomaterial> findInputBiomaterialsForProcess(Process process, Pageable pageable) {
        return biomaterialRepository.findByInputToProcessesContaining(process, pageable);
    }

    public Page<File> findInputFilesForProcess(Process process, Pageable pageable) {
        return fileRepository.findByInputToProcessesContaining(process, pageable);
    }

    public Page<Biomaterial> findOutputBiomaterialsForProcess(Process process, Pageable pageable) {
        return biomaterialRepository.findByDerivedByProcessesContaining(process, pageable);
    }

    public Page<File> findOutputFilesForProcess(Process process, Pageable pageable) {
        return fileRepository.findByDerivedByProcessesContaining(process, pageable);
    }

    public Process addProcessToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope,
                                                  Process process) {
        if(! process.getIsUpdate()) {
            projectRepository.findBySubmissionEnvelopesContains(submissionEnvelope).findFirst().ifPresent(process::setProject);
            return metadataCrudService.addToSubmissionEnvelopeAndSave(process, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(process, submissionEnvelope);
        }
    }

    // TODO Refactor this to use FileService
    // Implement logic to have the option to only create and createOrUpdate
    public Process addOutputFileToAnalysisProcess(final Process analysis, final File file) {
        SubmissionEnvelope submissionEnvelope = analysis.getSubmissionEnvelope();
        File targetFile = determineTargetFile(submissionEnvelope, file);
        targetFile.addToAnalysis(analysis);
        targetFile.setUuid(Uuid.newUuid());
        getFileRepository().save(targetFile);
        metadataDocumentEventHandler.handleMetadataDocumentCreate(targetFile);

        return analysis;
    }

    public Process addInputFileUuidToProcess(final Process process, final UUID inputFileUuid) {
        return fileRepository.findByUuidUuidAndIsUpdateFalse(inputFileUuid)
                             .map(inputFile -> addInputFileToProcess(process, inputFile))
                             .orElseThrow(() -> {
                                 throw new ResourceNotFoundException();
                             });
    }

    public Process addInputFileToProcess(final Process process, final File inputFile) {
        fileRepository.save(inputFile.addAsInputToProcess(process));
        return process;
    }

    private File determineTargetFile(SubmissionEnvelope submissionEnvelope, File file) {
        List<File> persistentFiles = fileRepository
                .findBySubmissionEnvelopeAndFileName(submissionEnvelope, file.getFileName());

        File targetFile = persistentFiles.stream().findFirst().orElse(file);
        return targetFile;
    }

    public Process addInputBundleManifest(final Process analysisProcess, BundleReference bundleReference) {
        for (String bundleUuid : bundleReference.getBundleUuids()) {
            Optional<BundleManifest> maybeBundleManifest = getBundleManifestRepository().findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid);
            maybeBundleManifest.ifPresentOrElse(analysisProcess::addInputBundleManifest, () -> {
                throw new ResourceNotFoundException(String.format("Could not find bundle with UUID %s", bundleUuid));
            });
        }

        return getProcessRepository().save(analysisProcess);
    }

    public Page<Process> findProcessesByInputBundleUuid(UUID bundleUuid, Pageable pageable) {
        Optional<BundleManifest> maybeBundleManifest = bundleManifestRepository.findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid.toString());

        return maybeBundleManifest.map(bundleManifest -> processRepository.findByInputBundleManifestsContaining(maybeBundleManifest.get(), pageable))
                                  .orElseThrow(() -> {
                                      throw new ResourceNotFoundException(String.format("Bundle with UUID %s not found", bundleUuid.toString()));
                                  });

    }

    /**
     *
     * Find all assay process IDs in a submission
     *
     * @param submissionEnvelope
     * @return A collection of IDs of every assay process in a submission
     */
    public Set<String> findAssays(SubmissionEnvelope submissionEnvelope) {
        Set<String> results = new LinkedHashSet<>();
        long fileStartTime = System.currentTimeMillis();

        long fileEndTime = System.currentTimeMillis();
        float fileQueryTime = ((float)(fileEndTime - fileStartTime)) / 1000;
        String fileQt = new DecimalFormat("#,###.##").format(fileQueryTime);
        getLog().info("Retrieving assays: file query time: {} s", fileQt);
        long allBioStartTime = System.currentTimeMillis();

        fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                      .forEach(derivedFile -> {
                          for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                              biomaterialRepository.findByInputToProcessesContains(derivedByProcess).findAny()
                                                   .ifPresent(__ -> results.add(derivedByProcess.getId()));
                          }
                      });

        long allBioEndTime = System.currentTimeMillis();
        float allBioQueryTime = ((float)(allBioEndTime - allBioStartTime)) / 1000;
        String allBioQt = new DecimalFormat("#,###.##").format(allBioQueryTime);
        getLog().info("Retrieving assays: biomaterial query time: {} s", allBioQt);
        return results;
    }

    /**
     *
     * Find all analysis process IDs in a submission
     *
     * @param submissionEnvelope
     * @return A collection of IDs of every analysis process in a submission
     */
    public Set<String> findAnalyses(SubmissionEnvelope submissionEnvelope) {
        Set<String> results = new LinkedHashSet<>();
        fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                      .forEach(derivedFile -> {
                          for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                              fileRepository.findByInputToProcessesContains(derivedByProcess).findAny()
                                            .ifPresent(__ -> results.add(derivedByProcess.getId()));
                          }
                      });
        return results;
    }

    public Process resolveBundleReferencesForProcess(Process analysis, BundleReference bundleReference) {
        for (String bundleUuid : bundleReference.getBundleUuids()) {
            Optional<BundleManifest> maybeBundleManifest = getBundleManifestRepository().findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid);

            maybeBundleManifest.ifPresentOrElse(bundleManifest -> {
                getLog().info("Adding bundle manifest link to process '" + analysis.getId() + "'");
                analysis.addInputBundleManifest(bundleManifest);
                Process savedAnalysis = getProcessRepository().save(analysis);

                // add the input files
                for (String fileUuid : bundleManifest.getFileFilesMap().keySet()) {
                    fileRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(fileUuid))
                                  .ifPresentOrElse(analysisInputFile -> {
                                      analysisInputFile.addAsInputToProcess(savedAnalysis);
                                      fileRepository.save(analysisInputFile);
                                  }, () -> {
                                      throw new ResourceNotFoundException(String.format("Could not find file with UUID %s", fileUuid));
                                  });


                }
            }, () -> {
                    throw new ResourceNotFoundException(String.format("Could not find bundle with UUID %s", bundleUuid));
            });
        }

        return getProcessRepository().save(analysis);
    }

    public Stream<Process> getProcesses(Collection<String> processIds) {
        return processRepository.findAllByIdIn(processIds);
    }
}
