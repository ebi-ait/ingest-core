package org.humancellatlas.ingest.file.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.state.StateEngine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
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
 * @date 06/09/17
 */
@RepositoryRestController
@ExposesResourceFor(File.class)
@RequiredArgsConstructor
@Getter

public class FileController {
    private final @NonNull FileService fileService;

    private final @NonNull
    ProcessRepository processRepository;

    private final @NonNull
    PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/files/{id}/inputFiles", method = RequestMethod.GET)
    ResponseEntity<?> getProcessByInputFiles(@PathVariable("id") File file,
                                    Pageable pageable,
                                    final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findByInputFiles(file, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
    }

    @RequestMapping(path = "/files/{id}/derivedFiles", method = RequestMethod.GET)
    ResponseEntity<?> getProcessByDerivedFiles(@PathVariable("id") File file,
                                    Pageable pageable,
                                    final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = getProcessRepository().findByDerivedFiles(file, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(processes, resourceAssembler));
    }

    private final @NonNull StateEngine stateEngine;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files",
                    method = RequestMethod.POST,
                    produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> addFileToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                  @RequestBody File file,
                                                  final PersistentEntityResourceAssembler assembler) {
        File entity = getFileService().addFileToSubmissionEnvelope(submissionEnvelope, file);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files/{id}",
            method = RequestMethod.PUT,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> linkFileToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                   @PathVariable("id") File file,
                                                  final PersistentEntityResourceAssembler assembler) {
        File entity = getFileService().addFileToSubmissionEnvelope(submissionEnvelope, file);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/files/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingFile(@PathVariable("id") File file) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getFileService().getFileRepository(),
                file,
                ValidationState.VALIDATING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/files/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateFile(@PathVariable("id") File file) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getFileService().getFileRepository(),
                file,
                ValidationState.VALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/files/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateFile(@PathVariable("id") File file) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getFileService().getFileRepository(),
                file,
                ValidationState.INVALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/files/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingFile(@PathVariable("id") File file) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getFileService().getFileRepository(),
                file,
                ValidationState.PROCESSING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/files/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeFile(@PathVariable("id") File file) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getFileService().getFileRepository(),
                file,
                ValidationState.COMPLETE);

        return ResponseEntity.accepted().body(event);
    }
}
