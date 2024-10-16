package uk.ac.ebi.subs.ingest.state;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.ValidationEvent;

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
