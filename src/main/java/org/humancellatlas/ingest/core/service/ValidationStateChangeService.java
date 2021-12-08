package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.state.ValidationStateEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ValidationStateChangeService {
    private final @NonNull MetadataCrudService metadataCrudService;

    private final @NonNull ValidationStateEventPublisher validationStateEventPublisher;

    public <T extends MetadataDocument> T changeValidationState(EntityType metadataType,
                                                  String metadataId,
                                                  ValidationState validationState) {
        T metadataDocument = metadataCrudService.setValidationState(metadataType, metadataId, validationState);
        validationStateEventPublisher.publishValidationStateChangeEventFor(metadataDocument);
        return metadataDocument;
    }
}