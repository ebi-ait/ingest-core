package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.core.service.strategy.impl.*;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.state.ValidationStateEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ValidationStateChangeService {
    private final @NonNull MetadataCrudService metadataCrudService;

    private final @NonNull ValidationStateEventPublisher validationStateEventPublisher;

    public MetadataDocument changeValidationState(String metadataType,
                                                              String metadataId,
                                                              ValidationState validationState) {
        MetadataCrudStrategy crudStrategy = metadataCrudService.crudStrategyForMetadataType(metadataType);
        MetadataDocument metadataDocument = crudStrategy.findMetadataDocument(metadataId);
        metadataDocument.setValidationState(validationState);
        metadataDocument = crudStrategy.saveMetadataDocument(metadataDocument);

        validationStateEventPublisher.publishValidationStateChangeEventFor(metadataDocument);

        return metadataDocument;
    }


}