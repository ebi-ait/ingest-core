package uk.ac.ebi.subs.ingest.submission.web;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.bundle.BundleManifest;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;
import uk.ac.ebi.subs.ingest.exporter.Exporter;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.process.ProcessService;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.protocol.ProtocolService;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.state.SubmitAction;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.study.Study;
import uk.ac.ebi.subs.ingest.study.StudyRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeService;
import uk.ac.ebi.subs.ingest.submission.SubmissionStateMachineService;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import uk.ac.ebi.subs.ingest.submissionmanifest.SubmissionManifest;
import uk.ac.ebi.subs.ingest.submissionmanifest.SubmissionManifestRepository;

/**
 * Spring controller that will handle submission events on a {@link SubmissionEnvelope}
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@RepositoryRestController
@ExposesResourceFor(SubmissionEnvelope.class)
@RequiredArgsConstructor
@Getter
public class SubmissionController {
  private final @NonNull Logger log = LoggerFactory.getLogger(getClass());
  private final @NonNull Exporter exporter;
  private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
  private final @NonNull SubmissionStateMachineService submissionStateMachineService;
  private final @NonNull ProcessService processService;
  private final @NonNull ProtocolService protocolService;
  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull FileRepository fileRepository;
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull StudyRepository studyRepository;
  private final @NonNull DatasetRepository datasetRepository;
  private final @NonNull ProtocolRepository protocolRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull BundleManifestRepository bundleManifestRepository;
  private final @NonNull SubmissionManifestRepository submissionManifestRepository;
  private final MessageRouter messageRouter;
  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

  @PostMapping("/submissionEnvelopes" + Links.UPDATE_SUBMISSION_URL)
  ResponseEntity<?> createUpdateSubmission(
      final PersistentEntityResourceAssembler resourceAssembler) {
    SubmissionEnvelope updateSubmission =
        getSubmissionEnvelopeService().createUpdateSubmissionEnvelope();

    return ResponseEntity.ok(resourceAssembler.toFullResource(updateSubmission));
  }

  @GetMapping({
    "/submissionEnvelopes/{sub_id}" + Links.PROJECTS_URL,
    "/submissionEnvelopes/{sub_id}" + Links.SUBMISSION_RELATED_PROJECTS_URL
  })
  ResponseEntity<?> getProjects(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Project> projects =
        getProjectRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(projects, resourceAssembler));
  }

  @GetMapping({
    "/submissionEnvelopes/{sub_id}" + Links.STUDIES_URL,
    "/submissionEnvelopes/{sub_id}" + Links.SUBMISSION_RELATED_STUDIES_URL
  })
  ResponseEntity<?> getStudies(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Study> studies =
        getStudyRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(studies, resourceAssembler));
  }

  @GetMapping({
    "/submissionEnvelopes/{sub_id}" + Links.DATASETS_URL,
    "/submissionEnvelopes/{sub_id}" + Links.SUBMISSION_RELATED_DATASETS_URL
  })
  ResponseEntity<?> getDatasets(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Dataset> datasets =
        getDatasetRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(datasets, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/biomaterials")
  ResponseEntity<?> getBiomaterials(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Biomaterial> biomaterials =
        getBiomaterialRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);

    return ResponseEntity.ok(
        getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/processes")
  ResponseEntity<?> getProcesses(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Process> processes =
        getProcessRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/protocols")
  ResponseEntity<?> getProtocols(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Protocol> protocols = protocolService.retrieve(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/files")
  ResponseEntity<?> getFiles(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<File> files = getFileRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/bundleManifests")
  ResponseEntity<?> getBundleManifests(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<BundleManifest> bundleManifests =
        getBundleManifestRepository()
            .findByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString(), pageable);

    return ResponseEntity.ok(
        getPagedResourcesAssembler().toResource(bundleManifests, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/submissionManifest")
  ResponseEntity<?> getSubmissionManifests(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Optional<SubmissionManifest> submissionManifest =
        Optional.ofNullable(
            getSubmissionManifestRepository()
                .findBySubmissionEnvelopeId(submissionEnvelope.getId()));
    if (submissionManifest.isPresent()) {
      return ResponseEntity.ok(resourceAssembler.toFullResource(submissionManifest.get()));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/biomaterials/{state}")
  ResponseEntity<?> getSamplesWithValidationState(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("state") String state,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Biomaterial> biomaterials =
        getBiomaterialRepository()
            .findBySubmissionEnvelopeAndValidationState(
                submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);

    return ResponseEntity.ok(
        getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/processes/{state}")
  ResponseEntity<?> getProcessesWithValidationState(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("state") String state,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Process> processes =
        getProcessRepository()
            .findBySubmissionEnvelopeAndValidationState(
                submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/protocols/{state}")
  ResponseEntity<?> getProtocolsWithValidationState(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("state") String state,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Protocol> protocols =
        getProtocolRepository()
            .findBySubmissionEnvelopeAndValidationState(
                submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
  }

  @GetMapping("/submissionEnvelopes/{sub_id}/files/{state}")
  ResponseEntity<?> getFilesWithValidationState(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("state") String state,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    Page<File> files =
        getFileRepository()
            .findBySubmissionEnvelopeAndValidationState(
                submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);

    return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.SUBMIT_URL)
  HttpEntity<?> submitEnvelopeRequest(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      @RequestBody(required = false) List<String> submitActionParam,
      final PersistentEntityResourceAssembler resourceAssembler) {
    List<SubmitAction> submitActions =
        Optional.of(
                submitActionParam.stream()
                    .map(submitAction -> SubmitAction.valueOf(submitAction.toUpperCase()))
                    .collect(Collectors.toList()))
            .orElse(List.of(SubmitAction.ARCHIVE, SubmitAction.EXPORT, SubmitAction.CLEANUP));

    submissionEnvelopeService.handleSubmitRequest(submissionEnvelope, submitActions);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.ARCHIVED_URL)
  HttpEntity<?> completeArchivingEnvelopeRequest(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    submissionEnvelopeService.handleEnvelopeStateUpdateRequest(
        submissionEnvelope, SubmissionState.ARCHIVED);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.EXPORT_URL)
  HttpEntity<?> exportEnvelopeRequest(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    submissionEnvelopeService.exportData(submissionEnvelope);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.CLEANUP_URL)
  HttpEntity<?> cleanupEnvelopeRequest(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    submissionEnvelopeService.handleEnvelopeStateUpdateRequest(
        submissionEnvelope, SubmissionState.CLEANUP);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.COMPLETE_URL)
  HttpEntity<?> completeEnvelopeRequest(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    submissionEnvelopeService.handleEnvelopeStateUpdateRequest(
        submissionEnvelope, SubmissionState.COMPLETE);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
  }

  private HttpEntity<?> enactStateTransition(
      SubmissionState state,
      SubmissionEnvelope envelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    envelope.enactStateTransition(state);
    getSubmissionEnvelopeRepository().save(envelope);

    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(envelope));
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_DRAFT_URL)
  public HttpEntity<?> enactDraftEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(SubmissionState.DRAFT, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_METADATA_INVALID_URL)
  HttpEntity<?> enactInvalidEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.METADATA_INVALID, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_METADATA_VALID_URL)
  HttpEntity<?> enactValidEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.METADATA_VALID, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_SUBMIT_URL)
  public HttpEntity<?> enactSubmitEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    HttpEntity<?> response =
        this.enactStateTransition(SubmissionState.SUBMITTED, submissionEnvelope, resourceAssembler);
    log.info(
        String.format("Submission envelope with ID %s was submitted.", submissionEnvelope.getId()));
    submissionEnvelopeService.handleCommitSubmit(submissionEnvelope);

    return response;
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_PROCESSING_URL)
  HttpEntity<?> enactProcessEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.PROCESSING, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_ARCHIVING_URL)
  HttpEntity<?> enactArchivingEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.ARCHIVING, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_ARCHIVED_URL)
  HttpEntity<?> enactArchivedEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    HttpEntity<?> response =
        this.enactStateTransition(SubmissionState.ARCHIVED, submissionEnvelope, resourceAssembler);
    log.info(
        String.format("Submission envelope with ID %s was archived.", submissionEnvelope.getId()));
    submissionEnvelopeService.handleCommitArchived(submissionEnvelope);

    return response;
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_EXPORTING_URL)
  HttpEntity<?> enactExportingEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.EXPORTING, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_EXPORTED_URL)
  HttpEntity<?> enactExportedEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    HttpEntity<?> response =
        this.enactStateTransition(SubmissionState.EXPORTED, submissionEnvelope, resourceAssembler);
    log.info(
        String.format("Submission envelope with ID %s was exported.", submissionEnvelope.getId()));
    submissionEnvelopeService.handleCommitExported(submissionEnvelope);

    return response;
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_CLEANUP_URL)
  HttpEntity<?> enactCleanupEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.CLEANUP, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_COMPLETE_URL)
  HttpEntity<?> enactCompleteEnvelope(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.COMPLETE, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_GRAPH_VALID_URL)
  HttpEntity<?> enactGraphValid(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.GRAPH_VALID, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.COMMIT_GRAPH_INVALID_URL)
  HttpEntity<?> enactGraphInvalid(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return this.enactStateTransition(
        SubmissionState.GRAPH_INVALID, submissionEnvelope, resourceAssembler);
  }

  private HttpEntity<?> performStateUpdateRequest(
      SubmissionState state,
      SubmissionEnvelope envelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    submissionEnvelopeService.handleEnvelopeStateUpdateRequest(envelope, state);
    return ResponseEntity.accepted().body(resourceAssembler.toFullResource(envelope));
  }

  /*@CheckAllowed(
      value = "#submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping("/submissionEnvelopes/{id}" + Links.GRAPH_VALIDATION_REQUESTED_URL)
  HttpEntity<?> requestGraphValidation(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    // Used by the user (UI) to start the validation process
    return this.performStateUpdateRequest(
        SubmissionState.GRAPH_VALIDATION_REQUESTED, submissionEnvelope, resourceAssembler);
  }*/

  @PutMapping("/submissionEnvelopes/{id}" + Links.GRAPH_VALID_URL)
  HttpEntity<?> requestGraphValid(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    // used by ingest-graph-validator to notify that graph is valid
    return this.performStateUpdateRequest(
        SubmissionState.GRAPH_VALID, submissionEnvelope, resourceAssembler);
  }

  @PutMapping("/submissionEnvelopes/{id}" + Links.GRAPH_INVALID_URL)
  HttpEntity<?> requestGraphInvalid(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      final PersistentEntityResourceAssembler resourceAssembler) {
    // used by ingest-graph-validator to notify that graph is invalid
    return this.performStateUpdateRequest(
        SubmissionState.GRAPH_INVALID, submissionEnvelope, resourceAssembler);
  }

  @GetMapping("/submissionEnvelopes/{id}" + Links.SUBMISSION_DOCUMENTS_SM_URL)
  ResponseEntity<?> getDocumentStateMachineReport(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope) {
    return ResponseEntity.ok(
        getSubmissionStateMachineService().documentStatesForEnvelope(submissionEnvelope));
  }

  @GetMapping("/submissionEnvelopes/{id}/sync")
  HttpEntity<?> forceStateCheck(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
    // TODO: if really needed, modify this method to ask the state tracker component for an update
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/submissionEnvelopes/{id}")
  public HttpEntity<?> forceDeleteSubmission(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope,
      @RequestParam(name = "force", required = false, defaultValue = "false") boolean forceDelete) {
    getSubmissionEnvelopeService().deleteSubmission(submissionEnvelope, forceDelete);

    return ResponseEntity.accepted().build();
  }

  @GetMapping("/submissionEnvelopes/{id}" + Links.SUBMISSION_CONTENT_LAST_UPDATED_URL)
  ResponseEntity<?> getContentLastUpdated(
      @PathVariable("id") SubmissionEnvelope submissionEnvelope) {
    Optional<Instant> lastUpdateDate =
        submissionEnvelopeService.getSubmissionContentLastUpdated(submissionEnvelope);

    return ResponseEntity.ok(
        Objects.requireNonNull(lastUpdateDate.map(Instant::toString).orElse(null)));
  }
}
