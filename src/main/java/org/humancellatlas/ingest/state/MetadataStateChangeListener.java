package org.humancellatlas.ingest.state;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    private final MessageRouter messageRouter;
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
        MetadataDocument document = event.getSource();
        messageRouter.routeValidationMessageFor(document);
    }


    @Override
    public void onBeforeConvert(BeforeConvertEvent<MetadataDocument> event) {
        MetadataDocument document = event.getSource();

//      TODO Ideally, this should be being set when the submission is submitted.
//      The exporter could set this. Putting this back here for now for convenience.
        if (!Optional.ofNullable(document.getDcpVersion()).isPresent()) {
            document.setDcpVersion(document.getSubmissionDate());
        }

        if (!Optional.ofNullable(document.getUuid()).isPresent()) {
            document.setUuid(Uuid.newUuid());
        }
    }
}
