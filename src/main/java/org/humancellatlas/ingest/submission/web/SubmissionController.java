package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.humancellatlas.ingest.state.SubmissionGraphValidationState;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.SubmitAction;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.humancellatlas.ingest.submission.SubmissionStateMachineService;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifest;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final @NonNull Exporter exporter;

    private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
    private final @NonNull SubmissionStateMachineService submissionStateMachineService;
    private final @NonNull ProcessService processService;
    private final @NonNull ProtocolService protocolService;

    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull BundleManifestRepository bundleManifestRepository;
    private final @NonNull SubmissionManifestRepository submissionManifestRepository;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;
    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());


    @RequestMapping(path = "/submissionEnvelopes" + Links.UPDATE_SUBMISSION_URL, method = RequestMethod.POST)
    ResponseEntity<?> createUpdateSubmission(
            final PersistentEntityResourceAssembler resourceAssembler) {
        SubmissionEnvelope updateSubmission = getSubmissionEnvelopeService().createUpdateSubmissionEnvelope();
        return ResponseEntity.ok(resourceAssembler.toFullResource(updateSubmission));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/projects", method = RequestMethod.GET)
    ResponseEntity<?> getProjects(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                  Pageable pageable,
                                  final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = getProjectRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(projects, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/relatedProjects", method = RequestMethod.GET)
    ResponseEntity<?> getRelatedProjects(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                  Pageable pageable,
                                  final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = getProjectRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(projects, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/biomaterials", method = RequestMethod.GET)
    ResponseEntity<?> getBiomaterials(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                      Pageable pageable,
                                      final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> biomaterials = getBiomaterialRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
    }


    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/processes", method = RequestMethod.GET)
    ResponseEntity<?> getProcesses(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes
                , resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.GET)
    ResponseEntity<?> getProtocols(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = protocolService.retrieve(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files", method = RequestMethod.GET)
    ResponseEntity<?> getFiles(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                               Pageable pageable,
                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findBySubmissionEnvelope(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/bundleManifests", method = RequestMethod.GET)
    ResponseEntity<?> getBundleManifests(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                         Pageable pageable,
                                         final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = getBundleManifestRepository().findByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString(), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(bundleManifests, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/submissionManifest", method = RequestMethod.GET)
    ResponseEntity<?> getSubmissionManifests(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                             final PersistentEntityResourceAssembler resourceAssembler) {
        Optional<SubmissionManifest> submissionManifest = Optional.ofNullable(getSubmissionManifestRepository().findBySubmissionEnvelopeId(submissionEnvelope.getId()));
        if (submissionManifest.isPresent()) {
            return ResponseEntity.ok(resourceAssembler.toFullResource(submissionManifest.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/biomaterials/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getSamplesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                    Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> biomaterials = getBiomaterialRepository().findBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/processes/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getProcessesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                      Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getProtocolsWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                      Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = getProtocolRepository().findBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getFilesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                  Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findBySubmissionEnvelopeAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.SUBMIT_URL, method = RequestMethod.PUT)
    HttpEntity<?> submitEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope,
                                        @RequestBody(required = false) List<String> submitActionParam,
                                        final PersistentEntityResourceAssembler resourceAssembler) {
        List<SubmitAction> submitActions = Optional.ofNullable(
                submitActionParam.stream().map(submitAction -> {
                    return SubmitAction.valueOf(submitAction.toUpperCase());
                }).collect(Collectors.toList())
        ).orElse(List.of(SubmitAction.ARCHIVE, SubmitAction.EXPORT, SubmitAction.CLEANUP));

        submissionEnvelopeService.handleSubmitRequest(submissionEnvelope, submitActions);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.ARCHIVED_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeArchivingEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.ARCHIVED);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.EXPORT_URL, method = RequestMethod.PUT)
    HttpEntity<?> exportEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope,
                                        final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.exportSubmission(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.CLEANUP_URL, method = RequestMethod.PUT)
    HttpEntity<?> cleanupEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.CLEANUP);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.COMPLETE);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_DRAFT_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactDraftEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.DRAFT);
        submissionEnvelope.enactGraphValidationStateTransition(SubmissionGraphValidationState.PENDING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactValidatingEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.VALIDATING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactInvalidEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.INVALID);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactValidEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.VALID);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_SUBMIT_URL,
            method = RequestMethod.PUT)
    HttpEntity<?> enactSubmitEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope,
                                      final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.SUBMITTED);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        log.info(String.format("Submission envelope with ID %s was submitted.", submissionEnvelope.getId()));
        submissionEnvelopeService.handleCommitSubmit(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactProcessEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.PROCESSING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_ARCHIVING_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactArchivingEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.ARCHIVING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_ARCHIVED_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactArchivedEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.ARCHIVED);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        log.info(String.format("Submission envelope with ID %s was archived.", submissionEnvelope.getId()));
        submissionEnvelopeService.handleCommitArchived(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_EXPORTING_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactExportingEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.EXPORTING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_EXPORTED_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactExportedEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.EXPORTED);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        log.info(String.format("Submission envelope with ID %s was exported.", submissionEnvelope.getId()));
        submissionEnvelopeService.handleCommitExported(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_CLEANUP_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactCleanupEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.CLEANUP);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactCompleteEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.COMPLETE);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.GRAPH_PENDING_URL, method = RequestMethod.PUT)
    HttpEntity<?> graphPendingRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleGraphValidationStateUpdateRequest(submissionEnvelope, SubmissionGraphValidationState.PENDING);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.GRAPH_VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> graphValidatingRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleGraphValidationStateUpdateRequest(submissionEnvelope, SubmissionGraphValidationState.VALIDATING);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.GRAPH_VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> graphValidRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleGraphValidationStateUpdateRequest(submissionEnvelope, SubmissionGraphValidationState.VALID);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.GRAPH_INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> graphInvalidRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleGraphValidationStateUpdateRequest(submissionEnvelope, SubmissionGraphValidationState.INVALID);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.SUBMISSION_DOCUMENTS_SM_URL, method = RequestMethod.GET)
    ResponseEntity<?> getDocumentStateMachineReport(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
        return ResponseEntity.ok(getSubmissionStateMachineService().documentStatesForEnvelope(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}/sync", method = RequestMethod.GET)
    HttpEntity<?> forceStateCheck(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
        // TODO: if really needed, modify this method to ask the state tracker component for an update
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}", method = RequestMethod.DELETE)
    HttpEntity<?> forceDeleteSubmission(@PathVariable("id") SubmissionEnvelope submissionEnvelope,
                                        @RequestParam(name = "force", required = false, defaultValue = "false") boolean forceDelete) {
        getSubmissionEnvelopeService().deleteSubmission(submissionEnvelope, forceDelete);
        return ResponseEntity.accepted().build();
    }
}
