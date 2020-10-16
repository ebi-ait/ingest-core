package org.humancellatlas.ingest.file.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileAlreadyExistsException;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.file.ValidationJob;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

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
    private final ProcessRepository processRepository;

    @NonNull
    private final PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files/{filename:.+}",
                                method = RequestMethod.POST,
                                produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> createFile(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                           @PathVariable("filename") String fileName,
                                           @RequestBody File file,
                                           final PersistentEntityResourceAssembler assembler) {
        try {
            File createdFile = fileService.createFile(fileName, file, submissionEnvelope);
            return ResponseEntity.accepted().body(assembler.toFullResource(createdFile));
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files",
                   method = RequestMethod.POST,
                   produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> addFileToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                  @RequestBody File file,
                                                  @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                  final PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            file.setUuid(new Uuid(uuid.toString()));
            file.setIsUpdate(true);
        });
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

    @RequestMapping(path = "files/{id}/inputBiomaterial/{input_id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addInputBiomaterial(@PathVariable("id") File file,
                                                    @PathVariable("input_id") Biomaterial inputBiomaterial,
                                                    @RequestBody Process process,
                                                    PersistentEntityResourceAssembler assembler) {
        fileService.addInputBiomaterial(file, process, inputBiomaterial);
        PersistentEntityResource resource = assembler.toFullResource(file);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "files/{id}/inputBiomaterials", method = RequestMethod.GET)
    ResponseEntity<PagedResources> getInputBiomaterials(@PathVariable("id") File file,
                                                    Pageable pageable,
                                                    PersistentEntityResourceAssembler resourceAssembler) {
        Page<Biomaterial> inputBiomaterials = fileService.getInputBiomaterials(file, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(inputBiomaterials, resourceAssembler));
    }

    @RequestMapping(path = "files/{id}/inputBiomaterials/{input_id}", method = RequestMethod.DELETE)
    ResponseEntity<Resource<?>> deleteInputBiomaterial(@PathVariable("id") File file,
                                                       @PathVariable("input_id") Biomaterial inputBiomaterial,
                                                       PersistentEntityResourceAssembler assembler) {
        fileService.deleteInputBiomaterial(file, inputBiomaterial);
        return ResponseEntity.accepted().build();

    }

}
