package org.humancellatlas.ingest.assay.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.assay.AssayService;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.state.StateEngine;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Assay.class)
@RequiredArgsConstructor
@Getter
public class AssayController {
    private final @NonNull AssayService assayService;
    private final @NonNull StateEngine stateEngine;
    
    private final @NonNull FileRepository fileRepository;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/assays", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addAssayToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                    @RequestBody Assay assay,
                                                    PersistentEntityResourceAssembler assembler) {
        Assay entity = getAssayService().addAssayToSubmissionEnvelope(submissionEnvelope, assay);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/assays/{id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkAssayToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                    @PathVariable("id") Assay assay,
                                                   PersistentEntityResourceAssembler assembler) {
        Assay entity = getAssayService().addAssayToSubmissionEnvelope(submissionEnvelope, assay);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/assays/{assay_id}/" + Links.FILE_REF_URL,
                    method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addFileReference(@PathVariable("analysis_id") Assay assay,
                                                 @RequestBody File file,
                                                 final PersistentEntityResourceAssembler assembler) {
        File entity = getFileRepository().save(file);
        Assay result = getAssayService().getAssayRepository().save(assay.addFile(entity));
        PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/assays/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingAssay(@PathVariable("id") Assay assay) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getAssayService().getAssayRepository(),
                assay,
                ValidationState.VALIDATING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/assays/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateAssay(@PathVariable("id") Assay assay) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getAssayService().getAssayRepository(),
                assay,
                ValidationState.VALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/assays/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateAssay(@PathVariable("id") Assay assay) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getAssayService().getAssayRepository(),
                assay,
                ValidationState.INVALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/assays/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingAssay(@PathVariable("id") Assay assay) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getAssayService().getAssayRepository(),
                assay,
                ValidationState.PROCESSING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/assays/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeAssay(@PathVariable("id") Assay assay) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getAssayService().getAssayRepository(),
                assay,
                ValidationState.COMPLETE);

        return ResponseEntity.accepted().body(event);
    }
}
