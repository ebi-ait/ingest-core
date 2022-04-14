package org.humancellatlas.ingest.process.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.*;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.*;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

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
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;

    private @Autowired
    ValidationStateChangeService validationStateChangeService;

    private @Autowired
    UriToEntityConversionService uriToEntityConversionService;

    private @Autowired
    MetadataLinkingService metadataLinkingService;

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


    @CheckAllowed(value = "#submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
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

    @CheckAllowed(value = "#submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
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
    ResponseEntity<Resource<?>> addBundleReference() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @CheckAllowed(value = "#analysis.submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/processes/{analysis_id}/" + Links.BUNDLE_REF_URL,
            method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> oldAddBundleReference(@PathVariable("analysis_id") Process analysis,
                                                      @RequestBody BundleReference bundleReference,
                                                      final PersistentEntityResourceAssembler assembler) {
        Process entity = getProcessService().resolveBundleReferencesForProcess(analysis, bundleReference);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#analysis.submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
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
    ResponseEntity<Resource<?>> addOutputFileReference() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @CheckAllowed(value = "#analysis.submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/processes/{analysis_id}/" + Links.FILE_REF_URL,
            method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> addOutputFileReference(@PathVariable("analysis_id") Process analysis,
                                                       @RequestBody File file,
                                                       final PersistentEntityResourceAssembler assembler) {
        Process result = processService.addOutputFileToAnalysisProcess(analysis, file);
        PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#analysis.submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
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

    @CheckAllowed(value = "#process.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PatchMapping(path = "/processes/{id}")
    HttpEntity<?> patchProcess(@PathVariable("id") Process process,
                               @RequestBody final ObjectNode patch,
                               PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content", "validationErrors", "graphValidationErrors");
        ObjectNode validPatch = patch.retain(allowedFields);
        Process updatedProcess = metadataUpdateService.update(process, validPatch);
        PersistentEntityResource resource = assembler.toFullResource(updatedProcess);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#process.submissionEnvelope.canAddTo()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/processes/{id}/protocols", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkProtocolsToProcess(@PathVariable("id") Process process,
                                         @RequestBody Resources<Object> incoming,
                                         HttpMethod requestMethod,
                                         PersistentEntityResourceAssembler assembler) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Protocol> protocols = uriToEntityConversionService.convertLinks(incoming.getLinks(), Protocol.class);
        metadataLinkingService.updateLinks(process, protocols, "protocols", requestMethod.equals(HttpMethod.PUT));
        return ResponseEntity.ok().build();
    }

    @CheckAllowed(value = "#process.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/processes/{id}/protocols/{protocolId}")
    HttpEntity<?> unlinkProtocolFromProcess(@PathVariable("id") Process process,
                                            @PathVariable("protocolId") Protocol protocol,
                                            PersistentEntityResourceAssembler assembler) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        metadataLinkingService.removeLink(process, protocol, "protocols");
        return ResponseEntity.noContent().build();
    }

    @CheckAllowed(value = "#process.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/processes/{id}")
    ResponseEntity<?> deleteProcess(@PathVariable("id") Process process) {
        metadataCrudService.deleteDocument(process);
        return ResponseEntity.noContent().build();
    }
}

