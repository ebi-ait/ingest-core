package org.humancellatlas.ingest.process.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.BundleReference;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
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
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "processes/{proc_id}/inputBiomaterials", method = RequestMethod.GET)
    ResponseEntity<?> getProcessInputBiomaterials(@PathVariable("proc_id") Process process,
                                                  Pageable pageable,
                                                  PersistentEntityResourceAssembler assembler) {
        Page<Biomaterial> inputBiomaterials = getProcessService().findInputBiomaterialsForProcess(process, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(inputBiomaterials, assembler));
    }

    @RequestMapping(path = "processes/{proc_id}/inputFiles", method = RequestMethod.GET)
    ResponseEntity<?> getProcessInputFiles(@PathVariable("proc_id") Process process,
                                           Pageable pageable,
                                           PersistentEntityResourceAssembler assembler) {
        Page<File> inputFiles = getProcessService().findInputFilesForProcess(process, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(inputFiles, assembler));
    }

    @RequestMapping(path = "processes/{proc_id}/derivedBiomaterials", method = RequestMethod.GET)
    ResponseEntity<?> getProcessOutputBiomaterials(@PathVariable("proc_id") Process process,
                                                   Pageable pageable,
                                                   PersistentEntityResourceAssembler assembler) {
        Page<Biomaterial> outputBiomaterials = getProcessService().findOutputBiomaterialsForProcess(process, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(outputBiomaterials, assembler));
    }

    @RequestMapping(path = "processes/{proc_id}/derivedFiles", method = RequestMethod.GET)
    ResponseEntity<?> getProcessOutputFiles(@PathVariable("proc_id") Process process,
                                            Pageable pageable,
                                            PersistentEntityResourceAssembler assembler) {
        Page<File> outputFiles = getProcessService().findOutputFilesForProcess(process, pageable);
        return ResponseEntity.ok(getPagedResourcesAssembler().toResource(outputFiles, assembler));
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

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL)
    ResponseEntity<Resource<?>> addBundleReference(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL,
                    method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addBundleReference(@PathVariable("analysis_id") Process analysis,
                                                   @RequestBody BundleReference bundleReference,
                                                   final PersistentEntityResourceAssembler assembler) {
        Process entity = getProcessService().resolveBundleReferencesForProcess(analysis, bundleReference);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.FILE_REF_URL)
    ResponseEntity<Resource<?>> addFileReference(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.FILE_REF_URL,
                    method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addFileReference(@PathVariable("analysis_id") Process analysis,
                                                 @RequestBody File file,
                                                 final PersistentEntityResourceAssembler assembler) {
        SubmissionEnvelope submissionEnvelope = analysis.getOpenSubmissionEnvelope();
        file.addToSubmissionEnvelope(submissionEnvelope);
        Process result = getProcessService().addFileToAnalysisProcess(analysis, file);
        PersistentEntityResource resource = assembler.toFullResource(result);
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

