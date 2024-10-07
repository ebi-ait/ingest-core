package uk.ac.ebi.subs.ingest.core.web;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.service.ValidationStateChangeService;
import uk.ac.ebi.subs.ingest.query.MetadataCriteria;
import uk.ac.ebi.subs.ingest.query.MetadataQueryService;
import uk.ac.ebi.subs.ingest.state.ValidationState;

@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(MetadataDocument.class)
public class MetadataController {
  private final @NonNull ValidationStateChangeService validationStateChangeService;
  private final @NonNull MetadataQueryService metadataQueryService;
  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

  @PutMapping("/{metadataType}/{id}" + Links.DRAFT_URL)
  HttpEntity<?> draftEvent(
      @PathVariable("metadataType") String metadataType,
      @PathVariable("id") String metadataId,
      PersistentEntityResourceAssembler assembler) {
    MetadataDocument metadataDocument =
        validationStateChangeService.changeValidationState(
            entityTypeForCollection(metadataType), metadataId, ValidationState.DRAFT);
    return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
  }

  @PutMapping("/{metadataType}/{id}" + Links.METADATA_VALIDATING_URL)
  HttpEntity<?> validatingEvent(
      @PathVariable("metadataType") String metadataType,
      @PathVariable("id") String metadataId,
      PersistentEntityResourceAssembler assembler) {
    MetadataDocument metadataDocument =
        validationStateChangeService.changeValidationState(
            entityTypeForCollection(metadataType), metadataId, ValidationState.VALIDATING);
    return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
  }

  @PutMapping("/{metadataType}/{id}" + Links.METADATA_VALID_URL)
  HttpEntity<?> validEvent(
      @PathVariable("metadataType") String metadataType,
      @PathVariable("id") String metadataId,
      PersistentEntityResourceAssembler assembler) {
    MetadataDocument metadataDocument =
        validationStateChangeService.changeValidationState(
            entityTypeForCollection(metadataType), metadataId, ValidationState.VALID);
    return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
  }

  @PutMapping("/{metadataType}/{id}" + Links.INVALID_URL)
  HttpEntity<?> invalidEvent(
      @PathVariable("metadataType") String metadataType,
      @PathVariable("id") String metadataId,
      PersistentEntityResourceAssembler assembler) {
    MetadataDocument metadataDocument =
        validationStateChangeService.changeValidationState(
            entityTypeForCollection(metadataType), metadataId, ValidationState.INVALID);
    return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
  }

  @PostMapping("/{metadataType}/query")
  ResponseEntity<PagedResources<Resource<?>>> query(
      @PathVariable("metadataType") String metadataType,
      @RequestBody List<MetadataCriteria> criteriaList,
      @RequestParam("operator") Optional<String> operator,
      Pageable pageable,
      final PersistentEntityResourceAssembler assembler) {
    Boolean andCriteria = operator.map("and"::equalsIgnoreCase).orElse(false);
    Page<?> docs =
        metadataQueryService.findByCriteria(
            entityTypeForCollection(metadataType), criteriaList, andCriteria, pageable);
    return ResponseEntity.ok(pagedResourcesAssembler.toResource(docs, assembler));
  }

  private EntityType entityTypeForCollection(String collection) {
    switch (collection) {
      case "biomaterials":
        return EntityType.BIOMATERIAL;
      case "protocols":
        return EntityType.PROTOCOL;
      case "projects":
        return EntityType.PROJECT;
      case "studies":
        return EntityType.STUDY;
      case "processes":
        return EntityType.PROCESS;
      case "files":
        return EntityType.FILE;
      default:
        throw new ResourceNotFoundException();
    }
  }
}