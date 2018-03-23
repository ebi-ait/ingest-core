package org.humancellatlas.ingest.submission.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;

import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull BundleManifestRepository bundleManifestRepository;


    private final @NonNull ProcessService processService;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files", method = RequestMethod.GET)
    ResponseEntity<?> getFiles(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                               Pageable pageable,
                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/projects", method = RequestMethod.GET)
    ResponseEntity<?> getProjects(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                  Pageable pageable,
                                  final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = getProjectRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(projects, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.GET)
    ResponseEntity<?> getProtocols(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = getProtocolRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/biomaterials", method = RequestMethod.GET)
    ResponseEntity<?> getBiomaterials(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
        Pageable pageable,
        final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> biomaterials = getBiomaterialRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/bundleManifests", method = RequestMethod.GET)
    ResponseEntity<?> getBundleManifests(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                      Pageable pageable,
                                      final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = getBundleManifestRepository().findByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString(), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(bundleManifests, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/processes", method = RequestMethod.GET)
    ResponseEntity<?> getProcesses(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
        Pageable pageable,
        final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findBySubmissionEnvelopesContaining(submissionEnvelope, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes
            , resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/biomaterials/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getSamplesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                    Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> biomaterials = getBiomaterialRepository().findBySubmissionEnvelopesContainingAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(biomaterials, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/processes/{state}", method = RequestMethod.GET)
        ResponseEntity<?> getProcessesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                    Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findBySubmissionEnvelopesContainingAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getProtocolsWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                      Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Protocol> protocols = getProtocolRepository().findBySubmissionEnvelopesContainingAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(protocols, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files/{state}", method = RequestMethod.GET)
    ResponseEntity<?> getFilesWithValidationState(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope, @PathVariable("state") String state,
                                                      Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<File> files = getFileRepository().findBySubmissionEnvelopesContainingAndValidationState(submissionEnvelope, ValidationState.valueOf(state.toUpperCase()), pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(files, resourceAssembler));
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/assays", method = RequestMethod.GET)
    ResponseEntity<?> getAssays(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> assays = getProcessService().retrieveAssaysFrom(submissionEnvelope, pageable);
        PagedResources body = getPagedResourcesAssembler().toResource(assays, resourceAssembler);
        return ResponseEntity.ok(body);
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/analyses", method = RequestMethod.GET)
    ResponseEntity<?> getAnalyses(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> analyses = getProcessService().retrieveAnalysesFrom(submissionEnvelope,
                pageable);
        PagedResources body = getPagedResourcesAssembler().toResource(analyses, resourceAssembler);
        return ResponseEntity.ok(body);
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.SUBMIT_URL, method = RequestMethod.PUT)
    HttpEntity<?> submitEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.SUBMITTED);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processEnvelopeRequest(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelopeService.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.PROCESSING);
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

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_SUBMIT_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactSubmitEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.SUBMITTED);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
        submissionEnvelopeService.triggerExportFor(submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionEnvelope));
    }

    @RequestMapping(path = "/submissionEnvelopes/{id}" + Links.COMMIT_PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> enactProcessEnvelope(@PathVariable("id") SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler resourceAssembler) {
        submissionEnvelope.enactStateTransition(SubmissionState.PROCESSING);
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
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


    @RequestMapping(path = "/submissionEnvelopes/{id}/sync", method = RequestMethod.GET)
    HttpEntity<?> forceStateCheck(@PathVariable("id") SubmissionEnvelope submissionEnvelope) {
        // TODO: if really needed, modify this method to ask the state tracker component for an update
        return ResponseEntity.noContent().build();
    }
}
