package uk.ac.ebi.subs.ingest.biomaterial.web;

import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialService;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataLinkingService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.core.service.UriToEntityConversionService;
import uk.ac.ebi.subs.ingest.core.service.ValidationStateChangeService;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;

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
  private @Autowired ValidationStateChangeService validationStateChangeService;
  private @Autowired UriToEntityConversionService uriToEntityConversionService;
  private @Autowired MetadataLinkingService metadataLinkingService;

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PostMapping(path = "submissionEnvelopes/{sub_id}/biomaterials")
  ResponseEntity<Resource<?>> addBiomaterialToEnvelope(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @RequestBody final Biomaterial biomaterial,
      @RequestParam("updatingUuid") final Optional<UUID> updatingUuid,
      final PersistentEntityResourceAssembler assembler) {
    updatingUuid.ifPresent(
        uuid -> {
          biomaterial.setUuid(new Uuid(uuid.toString()));
          biomaterial.setIsUpdate(true);
        });
    final Biomaterial entity =
        biomaterialService.addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
    final PersistentEntityResource resource = assembler.toFullResource(entity);

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "submissionEnvelopes/{sub_id}/biomaterials/{id}",
      method = RequestMethod.PUT)
  ResponseEntity<Resource<?>> linkBiomaterialToEnvelope(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @PathVariable("id") final Biomaterial biomaterial,
      final PersistentEntityResourceAssembler assembler) {
    final Biomaterial entity =
        biomaterialService.addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
    final PersistentEntityResource resource = assembler.toFullResource(entity);

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PatchMapping(path = "/biomaterials/{id}")
  HttpEntity<?> patchBiomaterial(
      @PathVariable("id") final Biomaterial biomaterial,
      @RequestBody final ObjectNode patch,
      final PersistentEntityResourceAssembler assembler) {
    final List<String> allowedFields =
        List.of("content", "validationErrors", "graphValidationErrors");
    final ObjectNode validPatch = patch.retain(allowedFields);
    final Biomaterial updatedBiomaterial = metadataUpdateService.update(biomaterial, validPatch);
    final PersistentEntityResource resource = assembler.toFullResource(updatedBiomaterial);

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "/biomaterials/{id}/inputToProcesses",
      method = {PUT, POST},
      consumes = {TEXT_URI_LIST_VALUE})
  HttpEntity<?> linkBiomaterialAsInputToProcesses(
      @PathVariable("id") final Biomaterial biomaterial,
      @RequestBody final Resources<Object> incoming,
      final HttpMethod requestMethod)
      throws URISyntaxException,
          InvocationTargetException,
          NoSuchMethodException,
          IllegalAccessException {
    final List<Process> processes =
        uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
    metadataLinkingService.updateLinks(
        biomaterial, processes, "inputToProcesses", requestMethod.equals(HttpMethod.PUT));

    return ResponseEntity.ok().build();
  }

  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @RequestMapping(
      path = "/biomaterials/{id}/derivedByProcesses",
      method = {PUT, POST},
      consumes = {TEXT_URI_LIST_VALUE})
  HttpEntity<?> linkBiomaterialAsDerivedByProcesses(
      @PathVariable("id") final Biomaterial biomaterial,
      @RequestBody final Resources<Object> incoming,
      final HttpMethod requestMethod)
      throws URISyntaxException,
          InvocationTargetException,
          NoSuchMethodException,
          IllegalAccessException {
    final List<Process> processes =
        uriToEntityConversionService.convertLinks(incoming.getLinks(), Process.class);
    metadataLinkingService.updateLinks(
        biomaterial, processes, "derivedByProcesses", requestMethod.equals(HttpMethod.PUT));

    return ResponseEntity.ok().build();
  }

  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @DeleteMapping(path = "/biomaterials/{id}/inputToProcesses/{processId}")
  HttpEntity<?> unlinkBiomaterialAsInputToProcesses(
      @PathVariable("id") final Biomaterial biomaterial,
      @PathVariable("processId") final Process process)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLink(biomaterial, process, "inputToProcesses");

    return ResponseEntity.noContent().build();
  }

  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @DeleteMapping(path = "/biomaterials/{id}/derivedByProcesses/{processId}")
  HttpEntity<?> unlinkBiomaterialAsDerivedProcesses(
      @PathVariable("id") final Biomaterial biomaterial,
      @PathVariable("processId") final Process process)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLink(biomaterial, process, "derivedByProcesses");

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(path = "/biomaterials/{id}")
  @CheckAllowed(
      value = "#biomaterial.submissionEnvelope.isEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  ResponseEntity<?> deleteBiomaterial(@PathVariable("id") final Biomaterial biomaterial)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    metadataLinkingService.removeLinks(biomaterial, "inputToProcesses");
    metadataLinkingService.removeLinks(biomaterial, "derivedByProcesses");
    metadataCrudService.deleteDocument(biomaterial);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/biomaterials/{parentId}/childBiomaterials")
  public ResponseEntity<Resource<?>> addChildBiomaterial(
      @PathVariable final String parentId,
      @RequestBody final Biomaterial childBiomaterial,
      final PersistentEntityResourceAssembler assembler) {
    biomaterialService.addChildBiomaterial(parentId, childBiomaterial);
    final PersistentEntityResource resource = assembler.toFullResource(childBiomaterial);

    return ResponseEntity.accepted().body(resource);
  }

  @DeleteMapping("/biomaterials/{parentId}/childBiomaterials/{childId}")
  public ResponseEntity<Resource<?>> removeChildBiomaterial(
      @PathVariable final String parentId,
      @PathVariable final String childId,
      final PersistentEntityResourceAssembler assembler) {
    final Biomaterial updatedParent = biomaterialService.removeChildBiomaterial(parentId, childId);
    final PersistentEntityResource resource = assembler.toFullResource(updatedParent);

    return ResponseEntity.accepted().body(resource);
  }

  @PostMapping("/biomaterials/{childId}/parentBiomaterials")
  public ResponseEntity<Resource<?>> addParentBiomaterial(
      @PathVariable final String childId,
      @RequestBody final Biomaterial parentBiomaterial,
      final PersistentEntityResourceAssembler assembler) {
    final Biomaterial updatedChild =
        biomaterialService.addParentBiomaterial(childId, parentBiomaterial);
    final PersistentEntityResource resource = assembler.toFullResource(updatedChild);

    return ResponseEntity.accepted().body(resource);
  }

  @DeleteMapping("/biomaterials/{childId}/parentBiomaterials/{parentId}")
  public ResponseEntity<Resource<?>> removeParentBiomaterial(
      @PathVariable final String childId,
      @PathVariable final String parentId,
      final PersistentEntityResourceAssembler assembler) {
    Biomaterial updatedChild = biomaterialService.removeParentBiomaterial(childId, parentId);
    final PersistentEntityResource resource = assembler.toFullResource(updatedChild);

    return ResponseEntity.accepted().body(resource);
  }
}