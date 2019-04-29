package org.humancellatlas.ingest.process;

import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
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
        process.setIsUpdate(submissionEnvelope.getIsUpdate());
        process.addToSubmissionEnvelope(submissionEnvelope);
        return getProcessRepository().save(process);
    }

    // TODO Refactor this to use FileService
    // Implement logic to have the option to only create and createOrUpdate
    public Process addFileToAnalysisProcess(final Process analysis, final File file) {
        SubmissionEnvelope submissionEnvelope = analysis.getOpenSubmissionEnvelope();
        File targetFile = determineTargetFile(submissionEnvelope, file);
        targetFile.addToAnalysis(analysis);
        getFileRepository().save(targetFile);
        metadataDocumentEventHandler.handleMetadataDocumentCreate(targetFile);

        return analysis;
    }

    private File determineTargetFile(SubmissionEnvelope submissionEnvelope, File file) {
        List<File> persistentFiles = fileRepository
                .findBySubmissionEnvelopesInAndFileName(submissionEnvelope, file.getFileName());

        File targetFile = persistentFiles.stream().findFirst().orElse(file);
        return targetFile;
    }

    public Process resolveBundleReferencesForProcess(Process analysis, BundleReference bundleReference) {
        for (String bundleUuid : bundleReference.getBundleUuids()) {
            BundleManifest bundleManifest = getBundleManifestRepository().findByBundleUuid(bundleUuid);
            if (bundleManifest != null) {
                getLog().info(String.format("Adding bundle manifest link to process '%s'", analysis.getId()));
                analysis.addInputBundleManifest(bundleManifest);
                analysis = getProcessRepository().save(analysis);

                // add the input files
                for (String fileUuid : bundleManifest.getFileFilesMap().keySet()) {
                    File analysisInputFile = fileRepository.findByUuid(new Uuid(fileUuid));
                    analysisInputFile.addAsInputToProcess(analysis);
                    fileRepository.save(analysisInputFile);
                }
            }
            else {
                getLog().warn(String.format(
                        "No Bundle Manifest present with bundle UUID '%s' - in future this will cause a critical error",
                        bundleUuid));
            }
        }
        return getProcessRepository().save(analysis);
    }


    public Page<Process> findProcessesByInputBundleUuid(UUID bundleUuid, Pageable pageable) {
        Optional<BundleManifest> maybeBundleManifest = Optional.ofNullable(bundleManifestRepository.findByBundleUuid(bundleUuid.toString()));

        if(maybeBundleManifest.isPresent()){
            return processRepository.findByInputBundleManifestsContaining(maybeBundleManifest.get(), pageable);
        } else {
            throw new ResourceNotFoundException(String.format("Bundle with UUID %s not found", bundleUuid.toString()));
        }
    }

    public Collection<Process> findAssays(SubmissionEnvelope submissionEnvelope) {
        Set<Process> results = new LinkedHashSet<>();
        long fileStartTime = System.currentTimeMillis();

        long fileEndTime = System.currentTimeMillis();
        float fileQueryTime = ((float)(fileEndTime - fileStartTime)) / 1000;
        String fileQt = new DecimalFormat("#,###.##").format(fileQueryTime);
        getLog().info("Retrieving assays: file query time: {} s", fileQt);
        long allBioStartTime = System.currentTimeMillis();

        fileRepository.findBySubmissionEnvelopesContains(submissionEnvelope)
                      .forEach(derivedFile -> {
                          for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                              if (!biomaterialRepository.findByInputToProcessesContains(derivedByProcess).isEmpty()) {
                                  results.add(derivedByProcess);
                              }
                          }
                      });

        long allBioEndTime = System.currentTimeMillis();
        float allBioQueryTime = ((float)(allBioEndTime - allBioStartTime)) / 1000;
        String allBioQt = new DecimalFormat("#,###.##").format(allBioQueryTime);
        getLog().info("Retrieving assays: biomaterial query time: {} s", allBioQt);
        return results;
    }

    public Collection<Process> findAnalyses(SubmissionEnvelope submissionEnvelope) {
        Set<Process> results = new LinkedHashSet<>();
        fileRepository.findBySubmissionEnvelopesContains(submissionEnvelope)
                      .forEach(derivedFile -> {
                          for (Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                              if (!fileRepository.findByInputToProcessesContains(derivedByProcess).isEmpty()) {
                                  results.add(derivedByProcess);
                              }
                          }
                      });
        return results;
    }
}
