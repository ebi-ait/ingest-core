package uk.ac.ebi.subs.ingest.submission;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.exception.StateTransitionNotAllowed;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.errors.SubmissionErrorRepository;
import uk.ac.ebi.subs.ingest.exporter.Exporter;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.patch.PatchRepository;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.project.ProjectService;
import uk.ac.ebi.subs.ingest.project.WranglingState;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.state.SubmitAction;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submissionmanifest.SubmissionManifestRepository;

@Service
@RequiredArgsConstructor
public class SubmissionEnvelopeService {
  @NonNull private final Logger log = LoggerFactory.getLogger(getClass());
  @NonNull private final MetadataCrudService metadataCrudService;
  @NonNull private final MessageRouter messageRouter;
  @NonNull private final Exporter exporter;
  @NonNull private final ExecutorService executorService = Executors.newFixedThreadPool(5);
  @NonNull private final SubmissionEnvelopeRepository submissionEnvelopeRepository;
  @NonNull private final SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;
  @NonNull private final SubmissionManifestRepository submissionManifestRepository;
  @NonNull private BundleManifestRepository bundleManifestRepository;
  @NonNull private ProjectRepository projectRepository;
  @NonNull private ProjectService projectService;
  @NonNull private ProcessRepository processRepository;
  @NonNull private ProtocolRepository protocolRepository;
  @NonNull private FileRepository fileRepository;
  @NonNull private BiomaterialRepository biomaterialRepository;
  @NonNull private PatchRepository patchRepository;
  @NonNull private SubmissionErrorRepository submissionErrorRepository;

  public void handleSubmitRequest(SubmissionEnvelope envelope, List<SubmitAction> submitActions) {
    getProject(envelope)
        .ifPresentOrElse(
            project -> {
              if (!project.getValidationState().equals(ValidationState.VALID)) {
                throw new StateTransitionNotAllowed(
                    String.format(
                        "Envelope with id %s cannot be submitted when the project is invalid.",
                        envelope.getId()));
              }
            },
            () -> {
              throw new StateTransitionNotAllowed(
                  String.format(
                      "Envelope with id %s cannot be submitted without a project.",
                      envelope.getId()));
            });

    if (envelope.getSubmissionState() != SubmissionState.GRAPH_VALID) {
      throw new StateTransitionNotAllowed(
          String.format(
              "Envelope with id %s cannot be submitted without a graph valid state",
              envelope.getId()));
    }

    if (isSubmitAction(submitActions)) {
      envelope.setSubmitActions(new HashSet<>(submitActions));
      submissionEnvelopeRepository.save(envelope);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Envelope with id %s and state %s is submitted without the required submit actions",
              envelope.getId(), envelope.getSubmissionState()));
    }
    handleEnvelopeStateUpdateRequest(envelope, SubmissionState.SUBMITTED);
  }

  public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope, SubmissionState state) {
    if (envelope.getSubmissionState() == state) {
      log.info(
          String.format(
              "No Need to transition submissionEnvelope: %s already in state: %s",
              envelope.getId(), envelope.getSubmissionState()));
    } else if (!envelope.allowedSubmissionStateTransitions().contains(state)) {
      throw new StateTransitionNotAllowed(
          String.format(
              "Envelope with id %s cannot be transitioned from state %s to state %s",
              envelope.getId(), envelope.getSubmissionState(), state));
    } else {
      /*messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);

      if (state == SubmissionState.GRAPH_VALIDATION_REQUESTED) {
        removeGraphValidationErrors(envelope);
      }*/

      envelope.enactStateTransition(state);
      submissionEnvelopeRepository.save(envelope);
    }
  }

  public void handleCommitSubmit(SubmissionEnvelope envelope) {
    Set<SubmitAction> submitActions = envelope.getSubmitActions();

    if (submitActions.isEmpty()) {
      log.info(
          String.format(
              "No Submit Actions for submission: %s in state: %s",
              envelope.getId(), envelope.getSubmissionState()));
    } else if (submitActions.contains(SubmitAction.ARCHIVE)) {
      handleEnvelopeStateUpdateRequest(envelope, SubmissionState.PROCESSING);
      archiveSubmission(envelope);
    } else {
      handleCommitArchived(envelope);
    }
  }

  public void handleCommitArchived(SubmissionEnvelope envelope) {
    Set<SubmitAction> submitActions = envelope.getSubmitActions();

    if (submitActions.contains(SubmitAction.EXPORT)) {
      handleEnvelopeStateUpdateRequest(envelope, SubmissionState.EXPORTING);
      exportData(envelope);
    } else if (submitActions.contains(SubmitAction.EXPORT_METADATA)) {
      handleEnvelopeStateUpdateRequest(envelope, SubmissionState.EXPORTING);
      generateSpreadsheet(envelope);
    } else {
      handleCommitExported(envelope);
    }
  }

  public void handleCommitExported(SubmissionEnvelope envelope) {
    getProject(envelope)
        .ifPresent(
            project -> projectService.updateWranglingState(project, WranglingState.SUBMITTED));

    if (envelope.getSubmitActions().contains(SubmitAction.CLEANUP)) {
      cleanupSubmission(envelope);
    }
  }

  private void archiveSubmission(SubmissionEnvelope envelope) {
    submit(exporter::exportManifests, envelope, "Archive Submission");
  }

  public void generateSpreadsheet(SubmissionEnvelope envelope) {
    submit(exporter::generateSpreadsheet, envelope, "Generate Spreadsheet");
  }

  public void exportData(SubmissionEnvelope envelope) {
    submit(exporter::exportData, envelope, "Export Data");
  }

  public void cleanupSubmission(SubmissionEnvelope envelope) {
    try {
      handleEnvelopeStateUpdateRequest(envelope, SubmissionState.CLEANUP);
    } catch (Exception e) {
      log.error(
          String.format(
              "Uncaught Exception sending message to cleanup upload area for submission %s",
              envelope.getId()),
          e);
    }
  }

  public SubmissionEnvelope createUpdateSubmissionEnvelope() {
    SubmissionEnvelope updateSubmissionEnvelope = new SubmissionEnvelope();

    submissionEnvelopeCreateHandler.setUuid(updateSubmissionEnvelope);
    updateSubmissionEnvelope.setIsUpdate(true);

    return submissionEnvelopeRepository.insert(updateSubmissionEnvelope);
  }

  public void deleteSubmission(SubmissionEnvelope submissionEnvelope, boolean forceDelete) {
    if (!(submissionEnvelope.isOpen() || forceDelete))
      throw new UnsupportedOperationException(
          "Cannot delete submission if it is already submitted!");

    RetryTemplate retry =
        RetryTemplate.builder()
            .maxAttempts(5)
            .fixedBackoff(75)
            .retryOn(OptimisticLockingFailureException.class)
            .build();
    retry.execute(
        context -> {
          cleanupLinksToSubmissionMetadata(submissionEnvelope);
          return null;
        });

    biomaterialRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    processRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    protocolRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    fileRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    bundleManifestRepository.deleteByEnvelopeUuid(
        submissionEnvelope.getUuid().getUuid().toString());
    patchRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    submissionManifestRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    submissionErrorRepository.deleteBySubmissionEnvelope(submissionEnvelope);
    submissionEnvelopeRepository.delete(submissionEnvelope);

    this.messageRouter.routeRequestUploadAreaCleanup(submissionEnvelope);
  }

  /**
   * Ensures that any links to metadata in the submission are removed.
   *
   * @param submissionEnvelope
   */
  private void cleanupLinksToSubmissionMetadata(SubmissionEnvelope submissionEnvelope) {
    long startTime = System.currentTimeMillis();

    processRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(metadataCrudService::removeLinksToDocument);

    protocolRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(metadataCrudService::removeLinksToDocument);

    bundleManifestRepository
        .findByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString())
        .forEach(
            bundleManifest ->
                processRepository
                    .findByInputBundleManifestsContains(bundleManifest)
                    .forEach(
                        process -> {
                          process.getInputBundleManifests().remove(bundleManifest);
                          processRepository.save(process);
                        }));

    fileRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(metadataCrudService::removeLinksToDocument);

    // project cleanup
    projectRepository
        .findBySubmissionEnvelope(submissionEnvelope)
        .forEach(
            project -> {
              project.setSubmissionEnvelope(
                  null); // TODO: address this; we should implement project containers that aren't
              // deleted as part of deleteSubmission()
              projectRepository.save(project);
            });

    projectRepository
        .findBySubmissionEnvelopesContains(submissionEnvelope)
        .forEach(
            project -> {
              project.getSubmissionEnvelopes().remove(submissionEnvelope);
              projectRepository.save(project);
            });

    long endTime = System.currentTimeMillis();
    float duration = ((float) (endTime - startTime)) / 1000;
    String durationStr = new DecimalFormat("#,###.##").format(duration);
    log.info("cleanup link time: {} s", durationStr);
  }

  private boolean isSubmitAction(List<SubmitAction> submitActions) {
    return submitActions.contains(SubmitAction.ARCHIVE)
        || submitActions.contains(SubmitAction.EXPORT)
        || submitActions.contains(SubmitAction.EXPORT_METADATA);
  }

  private void removeGraphValidationErrors(SubmissionEnvelope submissionEnvelope) {
    biomaterialRepository.saveAll(
        biomaterialRepository
            .findBySubmissionEnvelope(submissionEnvelope)
            .peek(biomaterial -> biomaterial.setGraphValidationErrors(new ArrayList<>()))
            .collect(Collectors.toList()));

    processRepository.saveAll(
        processRepository
            .findBySubmissionEnvelope(submissionEnvelope)
            .peek(process -> process.setGraphValidationErrors(new ArrayList<>()))
            .collect(Collectors.toList()));

    protocolRepository.saveAll(
        protocolRepository
            .findBySubmissionEnvelope(submissionEnvelope)
            .peek(protocol -> protocol.setGraphValidationErrors(new ArrayList<>()))
            .collect(Collectors.toList()));

    fileRepository.saveAll(
        fileRepository
            .findBySubmissionEnvelope(submissionEnvelope)
            .peek(file -> file.setGraphValidationErrors(new ArrayList<>()))
            .collect(Collectors.toList()));
  }

  public Optional<Instant> getSubmissionContentLastUpdated(SubmissionEnvelope submissionEnvelope) {
    PageRequest request = PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "updateDate"));
    List<Project> projects =
        projectRepository
            .findBySubmissionEnvelopesContaining(submissionEnvelope, request)
            .getContent();
    List<Biomaterial> biomaterials =
        biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope, request).getContent();
    List<Protocol> protocols =
        protocolRepository.findBySubmissionEnvelope(submissionEnvelope, request).getContent();
    List<Process> processes =
        processRepository.findBySubmissionEnvelope(submissionEnvelope, request).getContent();
    List<File> files =
        fileRepository.findBySubmissionEnvelope(submissionEnvelope, request).getContent();

    return Stream.of(projects, biomaterials, protocols, processes, files)
        .flatMap(List::stream)
        .map(MetadataDocument::getUpdateDate)
        .max(Instant::compareTo);
  }

  public Optional<Project> getProject(SubmissionEnvelope submissionEnvelope) {
    return projectRepository.findBySubmissionEnvelopesContains(submissionEnvelope).findFirst();
  }

  private void submit(
      Consumer<SubmissionEnvelope> submissionAction,
      SubmissionEnvelope submission,
      String actionName) {
    executorService.submit(
        () -> {
          try {
            submissionAction.accept(submission);
          } catch (Exception e) {
            log.error(
                String.format(
                    "Uncaught Exception sending message %s for Submission %s",
                    actionName, submission.getId()),
                e);
          }
        });
  }
}