package org.humancellatlas.ingest.state;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;


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
        if (document.getSubmissionEnvelope() != null) {
            messageRouter.routeStateTrackingUpdateMessageFor(document);
        }
    }
}
