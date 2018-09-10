package org.humancellatlas.ingest.state;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
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
    @Autowired @NonNull private final MessageRouter messageRouter;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
        MetadataDocument document = event.getSource();

        messageRouter.routeValidationMessageFor(document);
        messageRouter.routeAccessionMessageFor(document);
    }

}
