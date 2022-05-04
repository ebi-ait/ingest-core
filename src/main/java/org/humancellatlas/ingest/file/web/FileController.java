package org.humancellatlas.ingest.file.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.*;
import org.humancellatlas.ingest.file.*;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

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
    private final MetadataCrudService metadataCrudService;

    @NonNull
    private final MetadataUpdateService metadataUpdateService;

    private @Autowired
    ValidationStateChangeService validationStateChangeService;

    private @Autowired
    UriToEntityConversionService uriToEntityConversionService;

    private @Autowired
    MetadataLinkingService metadataLinkingService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/files",
            method = RequestMethod.POST,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> createFile(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                           @RequestBody File file,
                                           final PersistentEntityResourceAssembler assembler) {
        try {
            File createdFile = fileService.addFileToSubmissionEnvelope(submissionEnvelope, file);
            logFileDetails(submissionEnvelope, createdFile);
            return ResponseEntity.accepted().body(assembler.toFullResource(createdFile));
        } catch (FileAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }
    }

    private void logFileDetails(SubmissionEnvelope submissionEnvelope, File createdFile) {
        logger.info("submission uuid {}: created File: id {} uuid {} name {} dataFileUuid {}",
                submissionEnvelope.getUuid(),
                createdFile.getId(),
                createdFile.getUuid(),
                createdFile.getFileName(),
                createdFile.getDataFileUuid());
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

    @CheckAllowed(value = "#file.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PatchMapping(path = "/files/{id}")
    HttpEntity<?> patchFile(@PathVariable("id") File file,
                            @RequestBody final ObjectNode patch,
                            PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content", "fileName", "validationJob", "validationErrors", "graphValidationErrors", "fileArchiveResult");
        ObjectNode validPatch = patch.retain(allowedFields);
        File updatedFile = metadataUpdateService.update(file, validPatch);
        PersistentEntityResource resource = assembler.toFullResource(updatedFile);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#file.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/files/{id}/inputToProcesses", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkFileAsInputToProcesses(@PathVariable("id") File file,
                                             @RequestBody Resources<Object> incoming,
                                             HttpMethod requestMethod,
                                             PersistentEntityResourceAssembler assembler) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Process> processes = uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
        metadataLinkingService.updateLinks(file, processes, "inputToProcesses", requestMethod.equals(HttpMethod.PUT));

        return ResponseEntity.ok().build();
    }

    @CheckAllowed(value = "#file.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/files/{id}/derivedByProcesses", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkFileAsDerivedByProcesses(@PathVariable("id") File file,
                                               @RequestBody Resources<Object> incoming,
                                               HttpMethod requestMethod,
                                               PersistentEntityResourceAssembler assembler) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Process> processes = uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
        metadataLinkingService.updateLinks(file, processes, "derivedByProcesses", requestMethod.equals(HttpMethod.PUT));

        return ResponseEntity.ok().build();
    }

    @CheckAllowed(value = "#file.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/files/{id}/inputToProcesses/{processId}")
    HttpEntity<?> unlinkFileAsInputToProcesses(@PathVariable("id") File file,
                                               @PathVariable("processId") Process process,
                                               PersistentEntityResourceAssembler assembler) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        metadataLinkingService.removeLink(file, process, "inputToProcesses");
        return ResponseEntity.noContent().build();
    }

    @CheckAllowed(value = "#file.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/files/{id}/derivedByProcesses/{processId}")
    HttpEntity<?> unlinkFileAsDerivedByProcesses(@PathVariable("id") File file,
                                                 @PathVariable("processId") Process process,
                                                 PersistentEntityResourceAssembler assembler) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        metadataLinkingService.removeLink(file, process, "derivedByProcesses");
        return ResponseEntity.noContent().build();
    }

    @CheckAllowed(value = "#file.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/files/{id}")
    ResponseEntity<?> deleteFile(@PathVariable("id") File file) {
        metadataCrudService.deleteDocument(file);
        return ResponseEntity.noContent().build();
    }
}
