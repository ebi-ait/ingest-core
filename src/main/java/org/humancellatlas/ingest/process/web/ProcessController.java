package org.humancellatlas.ingest.process.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
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

import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Process.class)
@Getter
public class ProcessController {
  private final @NonNull ProcessService processService;


    @RequestMapping(path = "processes/{proc_id}/inputBiomaterials", method = RequestMethod.GET)
    ResponseEntity<Resource<?>> getProcessInputBiomaterials(@PathVariable("proc_id") Process process,
                                                            PersistentEntityResourceAssembler assembler){
        List<Biomaterial> inputBiomaterials = getProcessService().findInputBiomaterialsForProcess(process);
        return ResponseEntity.ok(assembler.toFullResource(inputBiomaterials));
    }

    @RequestMapping(path = "processes/{proc_id}/inputFiles", method = RequestMethod.GET)
    ResponseEntity<Resource<?>> getProcessInputFiles(@PathVariable("proc_id") Process process,
                                                            PersistentEntityResourceAssembler assembler){
        List<File> inputFiles = getProcessService().findInputFilesForProcess(process);
        return ResponseEntity.ok(assembler.toFullResource(inputFiles));
    }

    @RequestMapping(path = "processes/{proc_id}/derivedBiomaterials", method = RequestMethod.GET)
    ResponseEntity<Resource<?>> getProcessOutputBiomaterials(@PathVariable("proc_id") Process process,
                                                     PersistentEntityResourceAssembler assembler){
        List<Biomaterial> outputBiomaterials = getProcessService().findOutputBiomaterialsForProcess(process);
        return ResponseEntity.ok(assembler.toFullResource(outputBiomaterials));
    }

    @RequestMapping(path = "processes/{proc_id}/derivedFiles", method = RequestMethod.GET)
    ResponseEntity<Resource<?>> getProcessOutputFiles(@PathVariable("proc_id") Process process,
                                                     PersistentEntityResourceAssembler assembler){
        List<File> inputFiles = getProcessService().findInputFilesForProcess(process);
        return ResponseEntity.ok(assembler.toFullResource(inputFiles));
    }


    @RequestMapping(path = "submissionEnvelopes/{sub_id}/processes", method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addProcessToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @RequestBody Process process,
      PersistentEntityResourceAssembler assembler) {
    Process entity = getProcessService().addProcessToSubmissionEnvelope(submissionEnvelope, process);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/processes/{id}", method = RequestMethod.PUT)
  ResponseEntity<Resource<?>> linkProcessToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("id") Process process,
      PersistentEntityResourceAssembler assembler) {
    Process entity = getProcessService().addProcessToSubmissionEnvelope(submissionEnvelope, process);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "/processes/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
  HttpEntity<?> validatingProcess(@PathVariable("id") Process process,
          PersistentEntityResourceAssembler assembler) {
    process.setValidationState(ValidationState.VALIDATING);
    process = getProcessService().getProcessRepository().save(process);
    return ResponseEntity.accepted().body(assembler.toFullResource(process));
  }

  @RequestMapping(path = "/processes/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> validateProcess(@PathVariable("id") Process process,
          PersistentEntityResourceAssembler assembler) {
    process.setValidationState(ValidationState.VALID);
    process = getProcessService().getProcessRepository().save(process);
    return ResponseEntity.accepted().body(assembler.toFullResource(process));
  }

  @RequestMapping(path = "/processes/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> invalidateProcess(@PathVariable("id") Process process,
          PersistentEntityResourceAssembler assembler) {
      process.setValidationState(ValidationState.INVALID);
      process = getProcessService().getProcessRepository().save(process);
      return ResponseEntity.accepted().body(assembler.toFullResource(process));
  }

  @RequestMapping(path = "/processes/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
  HttpEntity<?> processingProcess(@PathVariable("id") Process process,
          PersistentEntityResourceAssembler assembler) {
      process.setValidationState(ValidationState.PROCESSING);
      process = getProcessService().getProcessRepository().save(process);
      return ResponseEntity.accepted().body(assembler.toFullResource(process));
  }

  @RequestMapping(path = "/processes/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
  HttpEntity<?> completeProcess(@PathVariable("id") Process process,
          PersistentEntityResourceAssembler assembler) {
      process.setValidationState(ValidationState.COMPLETE);
      process = getProcessService().getProcessRepository().save(process);
      return ResponseEntity.accepted().body(assembler.toFullResource(process));
  }

}

