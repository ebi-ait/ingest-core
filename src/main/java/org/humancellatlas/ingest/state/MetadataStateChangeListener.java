package org.humancellatlas.ingest.state;


import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessage;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.messaging.Constants.Exchanges;
import org.humancellatlas.ingest.messaging.Constants.Queues;
import org.humancellatlas.ingest.messaging.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Component
@RequiredArgsConstructor
@Getter
public class MetadataStateChangeListener extends AbstractMongoEventListener<MetadataDocument> {
    @Autowired @NonNull private final MessageSender messageSender;
    @Autowired @NonNull private final ResourceMappings resourceMappings;
    @Autowired @NonNull private final RepositoryRestConfiguration config;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
       // TODO: need to queue messages to components depending on nature of the change
        MetadataDocument document = event.getSource();
        MetadataDocumentMessage documentMessage = MetadataDocumentMessageBuilder.using(resourceMappings, config)
                                                                                .messageFor(document).build();

        maybePostMessageToStateTracker(document, documentMessage);
        maybePostMessageToAcessioner(document, documentMessage);
        maybePostMessageToValidator(document, documentMessage);
    }

    private boolean maybePostMessageToStateTracker(MetadataDocument document, MetadataDocumentMessage documentMessage) {
        // TODO: consider filtering whether the state tracker requires messages for every state change
        // let the state tracker know about everything for now
        getMessageSender().queueStateTrackingMessage(Exchanges.STATE_TRACKING_DIRECT, "", documentMessage);
        return true;
    }

    private boolean maybePostMessageToAcessioner(MetadataDocument document, MetadataDocumentMessage documentMessage) {
        // queue an accession message if the document has no uuid
        Optional<UUID> uuidOptional = Optional.ofNullable(document.getUuid().getUuid());
        if(uuidOptional.isPresent()) {
            getMessageSender().queueAccessionMessage(Exchanges.ACCESSION, Queues.ACCESSION_REQUIRED, documentMessage);
            return true;
        } else {
            return false;
        }
    }

    private boolean maybePostMessageToValidator(MetadataDocument metadataDocument, MetadataDocumentMessage documentMessage) {
        if(metadataDocument.getValidationState().equals(ValidationState.DRAFT)) {
            getMessageSender().queueValidationMessage(Exchanges.VALIDATION, Queues.VALIDATION_REQUIRED, documentMessage);
            return true;
        } else {
            return false;
        }
    }

}
