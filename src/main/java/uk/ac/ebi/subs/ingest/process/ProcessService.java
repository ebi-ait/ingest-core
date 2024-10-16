package uk.ac.ebi.subs.ingest.process;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import lombok.Getter;
import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.bundle.BundleManifest;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.state.MetadataDocumentEventHandler;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@Service
@Getter
public class ProcessService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired private SubmissionEnvelopeRepository submissionEnvelopeRepository;
  @Autowired private ProcessRepository processRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private BiomaterialRepository biomaterialRepository;
  @Autowired private BundleManifestRepository bundleManifestRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private MetadataCrudService metadataCrudService;
  @Autowired private MetadataUpdateService metadataUpdateService;
  @Autowired MetadataDocumentEventHandler metadataDocumentEventHandler;

  protected Logger getLog() {
    return log;
  }

  public Page<Biomaterial> findInputBiomaterialsForProcess(
      final Process process, final Pageable pageable) {
    return biomaterialRepository.findByInputToProcessesContaining(process, pageable);
  }

  public Page<File> findInputFilesForProcess(final Process process, final Pageable pageable) {
    return fileRepository.findByInputToProcessesContaining(process, pageable);
  }

  public Page<Biomaterial> findOutputBiomaterialsForProcess(
      final Process process, final Pageable pageable) {
    return biomaterialRepository.findByDerivedByProcessesContaining(process, pageable);
  }

  public Page<File> findOutputFilesForProcess(final Process process, final Pageable pageable) {
    return fileRepository.findByDerivedByProcessesContaining(process, pageable);
  }

  public Process addProcessToSubmissionEnvelope(
      final SubmissionEnvelope submissionEnvelope, final Process process) {
    if (!process.getIsUpdate()) {
      projectRepository
          .findBySubmissionEnvelopesContains(submissionEnvelope)
          .findFirst()
          .ifPresent(
              project -> {
                process.setProject(project);
                process.getProjects().add(project);
              });
      return metadataCrudService.addToSubmissionEnvelopeAndSave(process, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(process, submissionEnvelope);
    }
  }

  // TODO Refactor this to use FileService
  // Implement logic to have the option to only create and createOrUpdate
  public Process addOutputFileToAnalysisProcess(final Process analysis, final File file) {
    final SubmissionEnvelope submissionEnvelope = analysis.getSubmissionEnvelope();
    final File targetFile = determineTargetFile(submissionEnvelope, file);
    targetFile.addToAnalysis(analysis);
    targetFile.setUuid(Uuid.newUuid());
    getFileRepository().save(targetFile);
    metadataDocumentEventHandler.handleMetadataDocumentCreate(targetFile);

    return analysis;
  }

  public Process addInputFileUuidToProcess(final Process process, final UUID inputFileUuid) {
    return fileRepository
        .findByUuidUuidAndIsUpdateFalse(inputFileUuid)
        .map(inputFile -> addInputFileToProcess(process, inputFile))
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Process addInputFileToProcess(final Process process, final File inputFile) {
    fileRepository.save(inputFile.addAsInputToProcess(process));
    return process;
  }

  private File determineTargetFile(final SubmissionEnvelope submissionEnvelope, final File file) {
    final List<File> persistentFiles =
        fileRepository.findBySubmissionEnvelopeAndFileName(submissionEnvelope, file.getFileName());
    return persistentFiles.stream().findFirst().orElse(file);
  }

  public Process addInputBundleManifest(
      final Process analysisProcess, final BundleReference bundleReference) {
    for (final String bundleUuid : bundleReference.getBundleUuids()) {
      final Optional<BundleManifest> maybeBundleManifest =
          getBundleManifestRepository().findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid);
      maybeBundleManifest.ifPresentOrElse(
          analysisProcess::addInputBundleManifest,
          () -> {
            throw new ResourceNotFoundException(
                String.format("Could not find bundle with UUID %s", bundleUuid));
          });
    }
    return getProcessRepository().save(analysisProcess);
  }

  public Page<Process> findProcessesByInputBundleUuid(
      final UUID bundleUuid, final Pageable pageable) {
    final Optional<BundleManifest> maybeBundleManifest =
        bundleManifestRepository.findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid.toString());
    return maybeBundleManifest
        .map(
            bundleManifest ->
                processRepository.findByInputBundleManifestsContaining(bundleManifest, pageable))
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Bundle with UUID %s not found", bundleUuid.toString())));
  }

  /**
   * Find all assay process IDs in a submission
   *
   * @return A collection of IDs of every assay process in a submission
   */
  public Set<String> findAssays(final SubmissionEnvelope submissionEnvelope) {
    final Set<String> results = new LinkedHashSet<>();
    final long fileStartTime = System.currentTimeMillis();

    final long fileEndTime = System.currentTimeMillis();
    final float fileQueryTime = ((float) (fileEndTime - fileStartTime)) / 1000;
    final String fileQt = new DecimalFormat("#,###.##").format(fileQueryTime);
    getLog().info("Retrieving assays: file query time: {} s", fileQt);
    final long allBioStartTime = System.currentTimeMillis();

    fileRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(
            derivedFile -> {
              for (final Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                biomaterialRepository
                    .findByInputToProcessesContains(derivedByProcess)
                    .findAny()
                    .ifPresent(__ -> results.add(derivedByProcess.getId()));
              }
            });

    final long allBioEndTime = System.currentTimeMillis();
    final float allBioQueryTime = ((float) (allBioEndTime - allBioStartTime)) / 1000;
    final String allBioQt = new DecimalFormat("#,###.##").format(allBioQueryTime);
    getLog().info("Retrieving assays: biomaterial query time: {} s", allBioQt);
    return results;
  }

  /**
   * Find all analysis process IDs in a submission
   *
   * @return A collection of IDs of every analysis process in a submission
   */
  public Set<String> findAnalyses(final SubmissionEnvelope submissionEnvelope) {
    final Set<String> results = new LinkedHashSet<>();
    fileRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(
            derivedFile -> {
              for (final Process derivedByProcess : derivedFile.getDerivedByProcesses()) {
                fileRepository
                    .findByInputToProcessesContains(derivedByProcess)
                    .findAny()
                    .ifPresent(__ -> results.add(derivedByProcess.getId()));
              }
            });
    return results;
  }

  public Process resolveBundleReferencesForProcess(
      final Process analysis, final BundleReference bundleReference) {
    for (final String bundleUuid : bundleReference.getBundleUuids()) {
      final Optional<BundleManifest> maybeBundleManifest =
          getBundleManifestRepository().findTopByBundleUuidOrderByBundleVersionDesc(bundleUuid);
      maybeBundleManifest.ifPresentOrElse(
          bundleManifest -> {
            getLog().info("Adding bundle manifest link to process '" + analysis.getId() + "'");
            analysis.addInputBundleManifest(bundleManifest);
            final Process savedAnalysis = getProcessRepository().save(analysis);
            for (final String fileUuid : bundleManifest.getFileFilesMap().keySet()) {
              fileRepository
                  .findByUuidUuidAndIsUpdateFalse(UUID.fromString(fileUuid))
                  .ifPresentOrElse(
                      analysisInputFile -> {
                        analysisInputFile.addAsInputToProcess(savedAnalysis);
                        fileRepository.save(analysisInputFile);
                      },
                      () -> {
                        throw new ResourceNotFoundException(
                            String.format("Could not find file with UUID %s", fileUuid));
                      });
            }
          },
          () -> {
            throw new ResourceNotFoundException(
                String.format("Could not find bundle with UUID %s", bundleUuid));
          });
    }
    return getProcessRepository().save(analysis);
  }

  public Stream<Process> getProcesses(final Collection<String> processIds) {
    return processRepository.findAllByIdIn(processIds);
  }
}
