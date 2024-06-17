package org.humancellatlas.ingest.state;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidationStateEventPublisher {
  private final @NonNull ApplicationEventPublisher applicationEventPublisher;

  public void publishValidationStateChangeEventFor(MetadataDocument metadataDocument) {
    ValidationEvent validationEvent =
        new ValidationEvent(metadataDocument, metadataDocument.getValidationState().toString());
    applicationEventPublisher.publishEvent(validationEvent);
  }
}
