package org.humancellatlas.ingest.submission.state;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
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
public class SubmissionEnvelopeStateChangeListener extends AbstractMongoEventListener<MetadataDocument> {
    private final @NonNull SubmissionEnvelopeStateEngine submissionEnvelopeStateEngine;

    @Override public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
        MetadataDocument metadataDocument = event.getSource();
        SubmissionEnvelope envelope = metadataDocument.getSubmissionEnvelope();

        getSubmissionEnvelopeStateEngine().notifySubmissionEnvelopeOfMetadataDocumentChange(envelope, metadataDocument);
        getSubmissionEnvelopeStateEngine().analyseStateOfEnvelope(envelope);
    }
}
