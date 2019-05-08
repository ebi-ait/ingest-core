package org.humancellatlas.ingest.core.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(MetadataDocument.class)
public class MetadataController {
    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @RequestMapping(path = "/{metadataType}/{id}" + Links.DRAFT_URL, method = RequestMethod.PUT)
    HttpEntity<?> draftBiomaterial(@PathVariable("metadataType") String metadataType,
                                   @PathVariable("id") String metadataId,
                                   PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                                                                                               metadataId,
                                                                                               ValidationState.DRAFT);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingBiomaterial(@PathVariable("metadataType") String metadataType,
                                        @PathVariable("id") String metadataId,
                                        PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                                                                                               metadataId,
                                                                                               ValidationState.VALIDATING);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateBiomaterial(@PathVariable("metadataType") String metadataType,
                                      @PathVariable("id") String metadataId,
                                      PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                                                                                               metadataId,
                                                                                               ValidationState.VALID);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateBiomaterial(@PathVariable("metadataType") String metadataType,
                                        @PathVariable("id") String metadataId,
                                        PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(entityTypeForCollection(metadataType),
                                                                                               metadataId,
                                                                                               ValidationState.INVALID);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
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
