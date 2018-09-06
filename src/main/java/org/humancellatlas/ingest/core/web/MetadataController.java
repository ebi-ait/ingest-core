package org.humancellatlas.ingest.core.web;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@AllArgsConstructor
public class MetadataController {
    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @RequestMapping(path = "/{metadataType}/{id}" + Links.DRAFT_URL, method = RequestMethod.PUT)
    HttpEntity<?> draftBiomaterial(@PathVariable("metadataType") String metadataType,
                                   @PathVariable("id") String metadataId,
                                   PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.DRAFT);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingBiomaterial(@PathVariable("metadataType") String metadataType,
                                        @PathVariable("id") String metadataId,
                                        PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.VALIDATING);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateBiomaterial(@PathVariable("metadataType") String metadataType,
                                      @PathVariable("id") String metadataId,
                                      PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.VALID);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateBiomaterial(@PathVariable("metadataType") String metadataType,
                                        @PathVariable("id") String metadataId,
                                        PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.INVALID);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingBiomaterial(@PathVariable("metadataType") String metadataType,
                                        @PathVariable("id") String metadataId,
                                        PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.PROCESSING);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }

    @RequestMapping(path = "/{metadataType}/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeBiomaterial(@PathVariable("metadataType") String metadataType,
                                      @PathVariable("id") String metadataId,
                                      PersistentEntityResourceAssembler assembler) {
        MetadataDocument metadataDocument = validationStateChangeService.changeValidationState(metadataType,
                                                                                               metadataId,
                                                                                               ValidationState.COMPLETE);
        return ResponseEntity.accepted().body(assembler.toFullResource(metadataDocument));
    }
}
