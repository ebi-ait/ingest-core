package org.humancellatlas.ingest.biomaterial.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.*;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
@ExposesResourceFor(Biomaterial.class)
@Getter
public class BiomaterialController {

    private final @NonNull ProcessRepository processRepository;

    private final @NonNull BiomaterialService biomaterialService;

    private final @NonNull BiomaterialRepository biomaterialRepository;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    private final @NonNull MetadataCrudService metadataCrudService;

    private final @NonNull MetadataUpdateService metadataUpdateService;

    private @Autowired
    ValidationStateChangeService validationStateChangeService;

    private @Autowired
    UriToEntityConversionService uriToEntityConversionService;

    private @Autowired
    MetadataLinkingService metadataLinkingService;

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping(path = "submissionEnvelopes/{sub_id}/biomaterials")
    ResponseEntity<Resource<?>> addBiomaterialToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                         @RequestBody Biomaterial biomaterial,
                                                         @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                         PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            biomaterial.setUuid(new Uuid(uuid.toString()));
            biomaterial.setIsUpdate(true);
        });
        Biomaterial entity = getBiomaterialService().addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "submissionEnvelopes/{sub_id}/biomaterials/{id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkBiomaterialToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                          @PathVariable("id") Biomaterial biomaterial,
                                                          PersistentEntityResourceAssembler assembler) {
        Biomaterial entity = getBiomaterialService().addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PatchMapping(path = "/biomaterials/{id}")
    HttpEntity<?> patchBiomaterial(@PathVariable("id") Biomaterial biomaterial,
                                   @RequestBody final ObjectNode patch,
                                   PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content", "validationErrors", "graphValidationErrors");
        ObjectNode validPatch = patch.retain(allowedFields);
        Biomaterial updatedBiomaterial = metadataUpdateService.update(biomaterial, validPatch);
        PersistentEntityResource resource = assembler.toFullResource(updatedBiomaterial);
        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/biomaterials/{id}/inputToProcesses", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkBiomaterialAsInputToProcesses(@PathVariable("id") Biomaterial biomaterial,
                                                    @RequestBody Resources<Object> incoming,
                                                    HttpMethod requestMethod,
                                                    PersistentEntityResourceAssembler assembler) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Process> processes = uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
        metadataLinkingService.updateLinks(biomaterial, processes, "inputToProcesses", requestMethod.equals(HttpMethod.PUT));

        return ResponseEntity.ok().build();
    }

    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @RequestMapping(path = "/biomaterials/{id}/derivedByProcesses", method = {PUT, POST}, consumes = {TEXT_URI_LIST_VALUE})
    HttpEntity<?> linkBiomaterialAsDerivedByProcesses(@PathVariable("id") Biomaterial biomaterial,
                                                      @RequestBody Resources<Object> incoming,
                                                      HttpMethod requestMethod,
                                                      PersistentEntityResourceAssembler assembler) throws URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Process> processes = uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
        metadataLinkingService.updateLinks(biomaterial, processes, "derivedByProcesses", requestMethod.equals(HttpMethod.PUT));
        return ResponseEntity.ok().build();
    }

    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/biomaterials/{id}/inputToProcesses/{processId}")
    HttpEntity<?> unlinkBiomaterialAsInputToProcesses(@PathVariable("id") Biomaterial biomaterial,
                                                      @PathVariable("processId") Process process,
                                                      PersistentEntityResourceAssembler assembler) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        metadataLinkingService.removeLink(biomaterial, process, "inputToProcesses");
        return ResponseEntity.noContent().build();
    }

    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @DeleteMapping(path = "/biomaterials/{id}/derivedByProcesses/{processId}")
    HttpEntity<?> unlinkBiomaterialAsDerivedProcesses(@PathVariable("id") Biomaterial biomaterial,
                                                      @PathVariable("processId") Process process,
                                                      PersistentEntityResourceAssembler assembler) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        metadataLinkingService.removeLink(biomaterial, process, "derivedByProcesses");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/biomaterials/{id}")
    @CheckAllowed(value = "#biomaterial.submissionEnvelope.isEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    ResponseEntity<?> deleteBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
        metadataCrudService.deleteDocument(biomaterial);
        return ResponseEntity.noContent().build();
    }
}