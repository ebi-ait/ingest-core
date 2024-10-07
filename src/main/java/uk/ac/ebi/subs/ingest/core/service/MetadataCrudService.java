package uk.ac.ebi.subs.ingest.core.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.core.service.strategy.impl.*;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Service
@AllArgsConstructor
public class MetadataCrudService {
  private final @NonNull BiomaterialCrudStrategy biomaterialCrudStrategy;
  private final @NonNull ProcessCrudStrategy processCrudStrategy;
  private final @NonNull ProtocolCrudStrategy protocolCrudStrategy;
  private final @NonNull ProjectCrudStrategy projectCrudStrategy;
  private final @NonNull FileCrudStrategy fileCrudStrategy;
  private final @NonNull StudyCrudStrategy studyCrudStrategy;
  private final @NonNull DatasetCrudStrategy datasetCrudStrategy;

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
      case STUDY:
        return studyCrudStrategy;
      case DATASET:
        return datasetCrudStrategy;
      default:
        throw new RuntimeException(String.format("No such metadata type: %s", metadataType));
    }
  }

  public <T extends MetadataDocument> T save(T metadataDocument) {
    MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(metadataDocument.getType());
    return (T) crudStrategy.saveMetadataDocument(metadataDocument);
  }

  public <T extends MetadataDocument> T setValidationState(
      EntityType entityType, String entityId, ValidationState validationState) {
    MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(entityType);
    T document = (T) crudStrategy.findMetadataDocument(entityId);
    document.setValidationState(validationState);
    return (T) crudStrategy.saveMetadataDocument(document);
  }

  public <T extends MetadataDocument> T addToSubmissionEnvelopeAndSave(
      T metadataDocument, SubmissionEnvelope submissionEnvelope) {
    metadataDocument.setSubmissionEnvelope(submissionEnvelope);
    return (T)
        (crudStrategyForMetadataType(metadataDocument.getType())
            .saveMetadataDocument(metadataDocument));
  }

  public <T extends MetadataDocument> T findOriginalByUuid(String uuid, EntityType entityType) {
    return (T) crudStrategyForMetadataType(entityType).findOriginalByUuid(uuid);
  }

  public <T extends MetadataDocument> Collection<T> findAllBySubmission(
      SubmissionEnvelope submissionEnvelope, EntityType entityType) {
    return crudStrategyForMetadataType(entityType).findAllBySubmissionEnvelope(submissionEnvelope);
  }

  public <T extends MetadataDocument> void removeLinksToDocument(T metadataDocument) {
    crudStrategyForMetadataType(metadataDocument.getType()).removeLinksToDocument(metadataDocument);
  }

  public <T extends MetadataDocument> void deleteDocument(T metadataDocument) {
    crudStrategyForMetadataType(metadataDocument.getType()).deleteDocument(metadataDocument);
  }
}
