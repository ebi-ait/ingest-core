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
import org.humancellatlas.ingest.process.*;
import org.humancellatlas.ingest.process.Process;
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
    final ValidationStateChangeService validationStateChangeService;
    private @Autowired
    final UriToEntityConversionService uriToEntityConversionService;
    private @Autowired
    final MetadataLinkingService metadataLinkingService;

    // Input and Output Resources
    @GetMapping("processes/{proc_id}/inputBiomaterials")
    ResponseEntity<?> getProcessInputBiomaterials(@PathVariable("proc_id") final Process process,
                                                  final Pageable pageable,
                                                  final PersistentEntityResourceAssembler assembler) {
        final Page<Biomaterial> inputBiomaterials = processService.findInputBiomaterialsForProcess(process, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(inputBiomaterials, assembler));
    }

    @GetMapping("processes/{proc_id}/inputFiles")
    ResponseEntity<?> getProcessInputFiles(@PathVariable("proc_id") final Process process,
                                           final Pageable pageable,
                                           final PersistentEntityResourceAssembler assembler) {
        final Page<File> inputFiles = processService.findInputFilesForProcess(process, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(inputFiles, assembler));
    }

    @GetMapping("processes/{proc_id}/derivedBiomaterials")
    ResponseEntity<?> getProcessOutputBiomaterials(@PathVariable("proc_id") final Process process,
                                                   final Pageable pageable,
                                                   final PersistentEntityResourceAssembler assembler) {
        final Page<Biomaterial> outputBiomaterials = processService.findOutputBiomaterialsForProcess(process, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(outputBiomaterials, assembler));
    }

    @GetMapping("processes/{proc_id}/derivedFiles")
    ResponseEntity<?> getProcessOutputFiles(@PathVariable("proc_id") final Process process,
                                            final Pageable pageable,
                                            final PersistentEntityResourceAssembler assembler) {
        final Page<File> outputFiles = processService.findOutputFilesForProcess(process, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(outputFiles, assembler));
    }

    // Process Management
    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping("submissionEnvelopes/{sub_id}/processes")
    ResponseEntity<Resource<?>> addProcessToEnvelope(@PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
                                                     @RequestBody final Process process,
                                                     @RequestParam("updatingUuid") final Optional<UUID> updatingUuid,
                                                     final PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            process.setUuid(new Uuid(uuid.toString()));
            process.setIsUpdate(true);
        });
        final Process entity = processService.addProcessToSubmissionEnvelope(submissionEnvelope, process);
        final PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PutMapping("submissionEnvelopes/{sub_id}/processes/{id}")
    ResponseEntity<Resource<?>> linkProcessToEnvelope(@PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
                                                      @PathVariable("id") final Process process,
                                                      final PersistentEntityResourceAssembler assembler) {
        final Process entity = processService.addProcessToSubmissionEnvelope(submissionEnvelope, process);
        final PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @DeleteMapping("processes/{id}")
    @CheckAllowed(value = "#process.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<?> deleteProcess(@PathVariable("id") final Process process) {
        metadataCrudService.deleteDocument(process);
        return ResponseEntity.noContent().build();
    }

    // Bundle References
    @Deprecated
    @GetMapping("/processes/{analysis_id}/" + Links.BUNDLE_REF_URL)
    ResponseEntity<Resource<?>> addBundleReference() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PutMapping("/processes/{analysis_id}/" + Links.BUNDLE_REF_URL)
    @CheckAllowed(value = "#analysis.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<Resource<?>> oldAddBundleReference(@PathVariable("analysis_id") final Process analysis,
                                                      @RequestBody final BundleReference bundleReference,
                                                      final PersistentEntityResourceAssembler assembler) {
        final Process entity = processService.resolveBundleReferencesForProcess(analysis, bundleReference);
        final PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @PostMapping("/processes/{analysis_id}/" + Links.BUNDLE_REF_URL)
    @CheckAllowed(value = "#analysis.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<Resource<?>> addBundleReference(@PathVariable("analysis_id") final Process analysis,
                                                   @RequestBody final BundleReference bundleReference,
                                                   final PersistentEntityResourceAssembler assembler) {
        final Process entity = processService.addInputBundleManifest(analysis, bundleReference);
        final PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    // File References
    @GetMapping("/processes/{analysis_id}/" + Links.FILE_REF_URL)
    ResponseEntity<Resource<?>> addOutputFileReference() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PutMapping("/processes/{analysis_id}/" + Links.FILE_REF_URL)
    @CheckAllowed(value = "#analysis.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<Resource<?>> addOutputFileReference(@PathVariable("analysis_id") final Process analysis,
                                                       @RequestBody final File file,
                                                       final PersistentEntityResourceAssembler assembler) {
        final Process result = processService.addOutputFileToAnalysisProcess(analysis, file);
        final PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    @PostMapping("/processes/{analysis_id}/" + Links.INPUT_FILES_URL)
    @CheckAllowed(value = "#analysis.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<Resource<?>> addInputFileReference(@PathVariable("analysis_id") final Process analysis,
                                                      @RequestBody final InputFileReference inputFileReference,
                                                      final PersistentEntityResourceAssembler assembler) {
        final Process result = processService.addInputFileUuidToProcess(analysis, inputFileReference.getInputFileUuid());
        final PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    // Biomaterial References
    @PostMapping("/processes/{analysis_id}/" + Links.INPUT_BIOMATERIALS_URL)
    @CheckAllowed(value = "#analysis.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<Resource<?>> addInputBiomaterialReference(@PathVariable("analysis_id") final Process analysis,
                                                             @RequestBody final Biomaterial biomaterial,
                                                             final PersistentEntityResourceAssembler assembler) {
        final Process result = processService.addInputBiomaterialToProcess(analysis, biomaterial);
        final PersistentEntityResource resource = assembler.toFullResource(result);
        return ResponseEntity.accepted().body(resource);
    }

    // Protocol Management
    @CheckAllowed(value = "#process.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/processes/{id}/protocols", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkProtocolsToProcess(@PathVariable("id") Process process,
                                         @RequestBody Resources<Object> incoming,
                                         HttpMethod requestMethod) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Protocol> protocols = uriToEntityConversionService.convertLinks(incoming.getLinks(), Protocol.class);
        metadataLinkingService.updateLinks(process, protocols, "protocols", requestMethod.equals(HttpMethod.PUT));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/processes/{id}/protocols/{protocolId}")
    @CheckAllowed(value = "#process.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    HttpEntity<?> unlinkProtocolFromProcess(@PathVariable("id") final Process process,
                                            @PathVariable("protocolId") final Protocol protocol) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        metadataLinkingService.removeLink(process, protocol, "protocols");
        return ResponseEntity.noContent().build();
    }

    // Patch Process
    @PatchMapping("/processes/{id}")
    @CheckAllowed(value = "#process.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    HttpEntity<?> patchProcess(@PathVariable("id") final Process process,
                               @RequestBody final ObjectNode patch,
                               final PersistentEntityResourceAssembler assembler) {
        final List<String> allowedFields = List.of("content", "validationErrors", "graphValidationErrors");
        final ObjectNode validPatch = patch.retain(allowedFields);
        final Process updatedProcess = metadataUpdateService.update(process, validPatch);
        final PersistentEntityResource resource = assembler.toFullResource(updatedProcess);
        return ResponseEntity.accepted().body(resource);
    }

    // Find Processes by Input Bundle UUID
    @GetMapping("/processes/search/findByInputBundleUuid")
    ResponseEntity<?> findProcesessByInputBundleUuid(@RequestParam final String bundleUuid,
                                                     final Pageable pageable,
                                                     final PersistentEntityResourceAssembler resourceAssembler) {
        final Page<Process> processes = processService.findProcessesByInputBundleUuid(UUID.fromString(bundleUuid), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(processes, resourceAssembler));
    }
}
