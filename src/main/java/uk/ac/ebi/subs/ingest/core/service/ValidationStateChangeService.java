package uk.ac.ebi.subs.ingest.core.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.state.ValidationStateEventPublisher;

@Service
@AllArgsConstructor
public class ValidationStateChangeService {
  private final @NonNull MetadataCrudService metadataCrudService;

  private final @NonNull ValidationStateEventPublisher validationStateEventPublisher;

  public <T extends MetadataDocument> T changeValidationState(
      EntityType metadataType, String metadataId, ValidationState validationState) {
    T metadataDocument =
        metadataCrudService.setValidationState(metadataType, metadataId, validationState);
    validationStateEventPublisher.publishValidationStateChangeEventFor(metadataDocument);
    return metadataDocument;
  }
}
