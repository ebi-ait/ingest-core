package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.core.service.strategy.impl.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetadataCrudService {
    private final @NonNull BiomaterialCrudStrategy biomaterialCrudStrategy;
    private final @NonNull ProcessCrudStrategy processCrudStrategy;
    private final @NonNull ProtocolCrudStrategy protocolCrudStrategy;
    private final @NonNull ProjectCrudStrategy projectCrudStrategy;
    private final @NonNull FileCrudStrategy fileCrudStrategy;

    public MetadataCrudStrategy crudStrategyForMetadataType(EntityType metadataType) {
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

    public <T extends MetadataDocument> T addToSubmissionEnvelopeAndSave(T metadataDocument, SubmissionEnvelope submissionEnvelope) {
        metadataDocument.addToSubmissionEnvelope(submissionEnvelope);
        return (T) (crudStrategyForMetadataType(metadataDocument.getType()).saveMetadataDocument(metadataDocument));
    }
}
