package uk.ac.ebi.subs.ingest.file.web;

import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.service.*;
import uk.ac.ebi.subs.ingest.file.*;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;

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
@Validated
public class FileController {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @NonNull private final FileService fileService;
  @NonNull private final FileRepository fileRepository;
  @NonNull private final ProcessRepository processRepository;
  @NonNull private final PagedResourcesAssembler pagedResourcesAssembler;
  @NonNull private final MetadataCrudService metadataCrudService;
  @NonNull private final MetadataUpdateService metadataUpdateService;
  @Autowired private ValidationStateChangeService validationStateChangeService;
  @Autowired private UriToEntityConversionService uriToEntityConversionService;
  @Autowired private MetadataLinkingService metadataLinkingService;

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ResponseEntity<String> handleConstraintViolationException(final ConstraintViolationException e) {
    return new ResponseEntity<>(
        "not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "/submissionEnvelopes/{sub_id}/files",
      method = RequestMethod.POST,
      produces = MediaTypes.HAL_JSON_VALUE)
  ResponseEntity<Resource<?>> createFile(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @RequestBody @Valid final File file,
      final PersistentEntityResourceAssembler assembler) {
    try {
      final File createdFile = fileService.addFileToSubmissionEnvelope(submissionEnvelope, file);
      logFileDetails(submissionEnvelope, createdFile);

      return ResponseEntity.accepted().body(assembler.toFullResource(createdFile));
    } catch (FileAlreadyExistsException e) {
      throw new IllegalStateException(e);
    }
  }

  private void logFileDetails(final SubmissionEnvelope submissionEnvelope, final File createdFile) {
    logger.info(
        "submission uuid {}: created File: id {} uuid {} name {} dataFileUuid {}",
        submissionEnvelope.getUuid(),
        createdFile.getId(),
        createdFile.getUuid(),
        createdFile.getFileName(),
        createdFile.getDataFileUuid());
  }

  @RequestMapping(
      path = "/files/{id}/validationJob",
      method = RequestMethod.PUT,
      produces = MediaTypes.HAL_JSON_VALUE)
  ResponseEntity<Resource<?>> addFileValidationJob(
      @PathVariable("id") final File file,
      @RequestBody final ValidationJob validationJob,
      final PersistentEntityResourceAssembler assembler) {
    final File entity = fileService.addFileValidationJob(file, validationJob);
    final PersistentEntityResource resource = assembler.toFullResource(entity);

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PatchMapping(path = "/files/{id}")
  HttpEntity<?> patchFile(
      @PathVariable("id") final File file,
      @RequestBody final ObjectNode patch,
      final PersistentEntityResourceAssembler assembler) {
    final List<String> allowedFields =
        List.of(
            "content",
            "fileName",
            "validationJob",
            "validationErrors",
            "graphValidationErrors",
            "fileArchiveResult");
    final ObjectNode validPatch = patch.retain(allowedFields);
    final File updatedFile = metadataUpdateService.update(file, validPatch);
    final PersistentEntityResource resource = assembler.toFullResource(updatedFile);

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "/files/{id}/inputToProcesses",
      method = {PUT, POST},
      consumes = {TEXT_URI_LIST_VALUE})
  HttpEntity<?> linkFileAsInputToProcesses(
      @PathVariable("id") final File file,
      @RequestBody final Resources<Object> incoming,
      final HttpMethod requestMethod)
      throws URISyntaxException,
          InvocationTargetException,
          NoSuchMethodException,
          IllegalAccessException {

    final List<Process> processes =
        uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
    metadataLinkingService.updateLinks(
        file, processes, "inputToProcesses", requestMethod.equals(HttpMethod.PUT));

    return ResponseEntity.ok().build();
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "/files/{id}/derivedByProcesses",
      method = {PUT, POST},
      consumes = {TEXT_URI_LIST_VALUE})
  HttpEntity<?> linkFileAsDerivedByProcesses(
      @PathVariable("id") final File file,
      @RequestBody final Resources<Object> incoming,
      final HttpMethod requestMethod)
      throws URISyntaxException,
          InvocationTargetException,
          NoSuchMethodException,
          IllegalAccessException {

    final List<Process> processes =
        uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
    metadataLinkingService.updateLinks(
        file, processes, "derivedByProcesses", requestMethod.equals(HttpMethod.PUT));

    return ResponseEntity.ok().build();
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @DeleteMapping(path = "/files/{id}/inputToProcesses/{processId}")
  HttpEntity<?> unlinkFileAsInputToProcesses(
      @PathVariable("id") final File file,
      @PathVariable("processId") final Process process,
      final PersistentEntityResourceAssembler assembler)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLink(file, process, "inputToProcesses");

    return ResponseEntity.noContent().build();
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @DeleteMapping(path = "/files/{id}/derivedByProcesses/{processId}")
  HttpEntity<?> unlinkFileAsDerivedByProcesses(
      @PathVariable("id") final File file, @PathVariable("processId") final Process process)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLink(file, process, "derivedByProcesses");

    return ResponseEntity.noContent().build();
  }

  @CheckAllowed(
      value = "#file.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @DeleteMapping(path = "/files/{id}")
  ResponseEntity<?> deleteFile(@PathVariable("id") final File file)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLinks(file, "inputToProcesses");
    metadataLinkingService.removeLinks(file, "derivedByProcesses");
    metadataCrudService.deleteDocument(file);

    return ResponseEntity.noContent().build();
  }
}
