package uk.ac.ebi.subs.ingest.state;

import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;

@RepositoryEventHandler
@Component
@RequiredArgsConstructor
public class MetadataDocumentEventHandler {
  private final @NonNull MessageRouter messageRouter;

  @HandleAfterCreate
  public void metadataDocumentAfterCreate(MetadataDocument document) {
    this.handleMetadataDocumentCreate(document);
  }

  public void handleMetadataDocumentCreate(MetadataDocument document) {
    messageRouter.routeValidationMessageFor(document);
    // messageRouter.routeStateTrackingUpdateMessageFor(document);
  }
}
