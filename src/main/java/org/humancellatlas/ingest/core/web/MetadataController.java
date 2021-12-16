package org.humancellatlas.ingest.core.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.query.MetadataQueryService;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resource;

@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(MetadataDocument.class)
public class MetadataController {
    private final @NonNull ValidationStateChangeService validationStateChangeService;
    private final @NonNull MetadataQueryService metadataQueryService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @PutMapping("/{metadataType}/{id}" + Links.DRAFT_URL)
    HttpEntity<?> draftEvent(@PathVariable("metadataType") String metadataType,
                             @PathVariable("id") String metadataId,
                             PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                metadataId,
                ValidationState.DRAFT);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @PutMapping("/{metadataType}/{id}" + Links.METADATA_VALIDATING_URL)
    HttpEntity<?> validatingEvent(@PathVariable("metadataType") String metadataType,
                                  @PathVariable("id") String metadataId,
                                  PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                metadataId,
                ValidationState.VALIDATING);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @PutMapping("/{metadataType}/{id}" + Links.METADATA_VALID_URL)
    HttpEntity<?> validEvent(@PathVariable("metadataType") String metadataType,
                             @PathVariable("id") String metadataId,
                             PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                metadataId,
                ValidationState.VALID);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @PutMapping("/{metadataType}/{id}" + Links.INVALID_URL)
    HttpEntity<?> invalidEvent(@PathVariable("metadataType") String metadataType,
                               @PathVariable("id") String metadataId,
                               PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                metadataId,
                ValidationState.INVALID);
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
        Page<?> docs = metadataQueryService.findByCriteria(entityTypeForCollection(metadataType), criteriaList, andCriteria, pageable);
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
            case "processes":
                return EntityType.PROCESS;
            case "files":
                return EntityType.FILE;
            default:
                throw new ResourceNotFoundException();
        }
    }
}
