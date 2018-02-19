package org.humancellatlas.ingest.process.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.state.StateEngine;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
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
 * Created by rolando on 16/02/2018.
 */
@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Process.class)
@Getter
public class ProcessController {
  private final @NonNull ProcessService processService;
  private final @NonNull StateEngine stateEngine;

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/processs", method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addProcessToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @RequestBody Process process,
      PersistentEntityResourceAssembler assembler) {
    Process entity = getProcessService().addProcessToSubmissionEnvelope(submissionEnvelope, process);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/processs/{id}", method = RequestMethod.PUT)
  ResponseEntity<Resource<?>> linkProcessToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("id") Process process,
      PersistentEntityResourceAssembler assembler) {
    Process entity = getProcessService().addProcessToSubmissionEnvelope(submissionEnvelope, process);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "/processs/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
  HttpEntity<?> validatingProcess(@PathVariable("id") Process process) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getProcessService().getProcessRepository(),
        process,
        ValidationState.VALIDATING);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/processs/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> validateProcess(@PathVariable("id") Process process) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getProcessService().getProcessRepository(),
        process,
        ValidationState.VALID);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/processs/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> invalidateProcess(@PathVariable("id") Process process) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getProcessService().getProcessRepository(),
        process,
        ValidationState.INVALID);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/processs/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
  HttpEntity<?> processingProcess(@PathVariable("id") Process process) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getProcessService().getProcessRepository(),
        process,
        ValidationState.PROCESSING);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/processs/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
  HttpEntity<?> completeProcess(@PathVariable("id") Process process) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getProcessService().getProcessRepository(),
        process,
        ValidationState.COMPLETE);

    return ResponseEntity.accepted().body(event);
  }
}

