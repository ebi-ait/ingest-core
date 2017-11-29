package org.humancellatlas.ingest.state;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final @NonNull StateEngine stateEngine;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
        try {
            MetadataDocument metadataDocument = event.getSource();
            SubmissionEnvelope envelope = metadataDocument.getOpenSubmissionEnvelope();

            SubmissionEnvelope latestEnvelope =
                    this.getStateEngine().notifySubmissionEnvelopeOfMetadataDocumentChange(envelope, metadataDocument);
            this.getStateEngine().analyseStateOfEnvelope(latestEnvelope)
                    .ifPresent(event1 -> getLog().debug("Event triggered on submission envelope", event1));
        }
        catch (Exception e) {
            getLog().error("Save propagation error", e);
        }
    }
}
