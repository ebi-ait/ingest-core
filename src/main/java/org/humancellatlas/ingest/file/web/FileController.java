package org.humancellatlas.ingest.file.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.*;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @NonNull
    private final FileService fileService;

    @NonNull
    private final FileRepository fileRepository;

    @NonNull
    private final ProcessRepository processRepository;

    @NonNull
    private final PagedResourcesAssembler pagedResourcesAssembler;

    @NonNull
    private final MetadataUpdateService metadataUpdateService;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files",
            method = RequestMethod.POST,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> createFile(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                           @RequestBody File file,
                                           final PersistentEntityResourceAssembler assembler) {
        try {
            File createdFile = fileService.addFileToSubmissionEnvelope(submissionEnvelope, file);
            return ResponseEntity.accepted().body(assembler.toFullResource(createdFile));
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequestMapping(path = "/files/{id}/validationJob",
            method = RequestMethod.PUT,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> addFileValidationJob(@PathVariable("id") File file,
                                                     @RequestBody ValidationJob validationJob,
                                                     final PersistentEntityResourceAssembler assembler) {
        File entity = getFileService().addFileValidationJob(file, validationJob);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @PatchMapping(path = "/files/{id}")
    HttpEntity<?> patchFile(@PathVariable("id") File file,
                            @RequestBody final ObjectNode patch,
                            PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content", "fileName", "validationJob", "validationErrors");
        ObjectNode validPatch = patch.retain(allowedFields);
        File updatedFile = metadataUpdateService.update(file, validPatch);
        PersistentEntityResource resource = assembler.toFullResource(updatedFile);
        return ResponseEntity.accepted().body(resource);
    }
}
