package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.core.service.strategy.impl.*;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class MetadataCrudService {
    private final @NonNull BiomaterialCrudStrategy biomaterialCrudStrategy;
    private final @NonNull ProcessCrudStrategy processCrudStrategy;
    private final @NonNull ProtocolCrudStrategy protocolCrudStrategy;
    private final @NonNull ProjectCrudStrategy projectCrudStrategy;
    private final @NonNull FileCrudStrategy fileCrudStrategy;

    private MetadataCrudStrategy crudStrategyForMetadataType(EntityType metadataType) {
        switch (metadataType) {
            case BIOMATERIAL:
                return biomaterialCrudStrategy;
            case PROCESS:
                return processCrudStrategy;
            case PROTOCOL:
                return protocolCrudStrategy;
            case PROJECT:
                return projectCrudStrategy;
            case FILE:
                return fileCrudStrategy;
            default:
                throw new RuntimeException(String.format("No such metadata type: %s", metadataType));
        }
    }

    public  <T extends MetadataDocument> T save(T metadataDocument) {
        MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(metadataDocument.getType());
        return (T) crudStrategy.saveMetadataDocument(metadataDocument);
    }

    public <T extends MetadataDocument> T setValidationState(EntityType entityType, String entityId, ValidationState validationState) {
        MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(entityType);
        T document = (T) crudStrategy.findMetadataDocument(entityId);
        document.setValidationState(validationState);
        return (T) crudStrategy.saveMetadataDocument(document);

    }

    public <T extends MetadataDocument> T addToSubmissionEnvelopeAndSave(T metadataDocument, SubmissionEnvelope submissionEnvelope) {
        if(! Optional.ofNullable(metadataDocument.getUuid()).isPresent()) {
            metadataDocument.setUuid(Uuid.newUuid());
        }
        metadataDocument.addToSubmissionEnvelope(submissionEnvelope);
        return (T) (crudStrategyForMetadataType(metadataDocument.getType()).saveMetadataDocument(metadataDocument));
    }

    public <T extends MetadataDocument> T findOriginalByUuid(String uuid, EntityType entityType) {
        return (T) crudStrategyForMetadataType(entityType).findOriginalByUuid(uuid);
    }

    public <T extends MetadataDocument> Stream<T> findBySubmission(SubmissionEnvelope submissionEnvelope, EntityType entityType) {
        return crudStrategyForMetadataType(entityType).findBySubmissionEnvelope(submissionEnvelope);
    }
}
