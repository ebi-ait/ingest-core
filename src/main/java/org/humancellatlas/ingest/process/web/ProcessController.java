package org.humancellatlas.ingest.process.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.*;
import org.humancellatlas.ingest.protocol.Protocol;
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
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by rolando on 16/02/2018.
 */
@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Process.class)
@Getter
public class ProcessController {
    private final @NonNull ProcessService processService;
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;
    private final @NonNull JsonPatcher jsonPatcher;

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
                                                     @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                     PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            process.setUuid(new Uuid(uuid.toString()));
            process.setIsUpdate(true);
        });
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

    @Deprecated
    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL)
    ResponseEntity<Resource<?>> addBundleReference(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL,
                    method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> oldAddBundleReference(@PathVariable("analysis_id") Process analysis,
                                                   @RequestBody BundleReference bundleReference,
                                                   final PersistentEntityResourceAssembler assembler) {
        Process entity = getProcessService().resolveBundleReferencesForProcess(analysis, bundleReference);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL,
            method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addBundleReference(@PathVariable("analysis_id") Process analysis,
                                                   @RequestBody BundleReference bundleReference,
                                                   final PersistentEntityResourceAssembler assembler) {
        Process entity = getProcessService().addInputBundleManifest(analysis, bundleReference);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.FILE_REF_URL)
    ResponseEntity<Resource<?>> addOutputFileReference(){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.FILE_REF_URL,
                    method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addOutputFileReference(@PathVariable("analysis_id") Process analysis,
                                                       @RequestBody File file,
                                                       final PersistentEntityResourceAssembler assembler) {
        Process result = processService.addOutputFileToAnalysisProcess(analysis, file);
        PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/processes/{analysis_id}/" + Links.INPUT_FILES_URL,
            method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addInputFileReference(@PathVariable("analysis_id") Process analysis,
                                                      @RequestBody InputFileReference inputFileReference,
                                                      final PersistentEntityResourceAssembler assembler) {
        Process result = processService.addInputFileUuidToProcess(analysis, inputFileReference.getInputFileUuid());
        PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/processes/search/findByInputBundleUuid", method = RequestMethod.GET)
    ResponseEntity<?> findProcesessByInputBundleUuid(@RequestParam String bundleUuid,
                                                     Pageable pageable,
                                                     final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Process> processes = processService.findProcessesByInputBundleUuid(UUID.fromString(bundleUuid), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(processes, resourceAssembler));
    }

    @RequestMapping(path = "/processes/{id}", method = RequestMethod.PATCH)
    HttpEntity<?> patchProcess(@PathVariable("id") Process process,
                               @RequestBody final ObjectNode patch,
                               PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content");
        ObjectNode validPatch = patch.retain(allowedFields);
        Process patchedProcess = jsonPatcher.merge(validPatch, process);

        Process entity = processRepository.save(patchedProcess);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return  ResponseEntity.accepted().body(resource);
    }
}

