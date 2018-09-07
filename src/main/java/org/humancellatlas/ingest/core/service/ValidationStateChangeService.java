package org.humancellatlas.ingest.core.service;

import lombok.*;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.core.service.strategy.impl.*;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.state.ValidationStateEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ValidationStateChangeService {
    private final @NonNull BiomaterialCrudStrategy biomaterialCrudStrategy;
    private final @NonNull ProcessCrudStrategy processCrudStrategy;
    private final @NonNull ProtocolCrudStrategy protocolCrudStrategy;
    private final @NonNull ProjectCrudStrategy projectCrudStrategy;
    private final @NonNull FileCrudStrategy fileCrudStrategy;

    private final @NonNull ValidationStateEventPublisher validationStateEventPublisher;

    public MetadataDocument changeValidationState(String metadataType,
                                                              String metadataId,
                                                              ValidationState validationState) {
        MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(metadataType);
        MetadataDocument metadataDocument = crudStrategy.findMetadataDocument(metadataId);
        metadataDocument.setValidationState(validationState);
        metadataDocument = crudStrategy.saveMetadataDocument(metadataDocument);

        validationStateEventPublisher.publishValidationStateChangeEventFor(metadataDocument);

        return metadataDocument;
    }


    private MetadataCrudStrategy crudStrategyForMetadataType(String metadataType) {
        switch (metadataType) {
            case "biomaterials":
                return biomaterialCrudStrategy;
            case "processes":
                return processCrudStrategy;
            case "protocols":
                return protocolCrudStrategy;
            case "projects":
                return projectCrudStrategy;
            case "files":
                return fileCrudStrategy;
            default:
                throw new RuntimeException(String.format("No such metadata type: %s", metadataType));
        }
    }

}