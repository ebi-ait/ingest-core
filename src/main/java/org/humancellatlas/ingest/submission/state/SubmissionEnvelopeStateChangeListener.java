package org.humancellatlas.ingest.submission.state;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
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
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Override public void onAfterSave(AfterSaveEvent<MetadataDocument> event) {
        System.out.println("Doc saved!");
        MetadataDocument metadataDocument = event.getSource();
        SubmissionEnvelope envelope = metadataDocument.getSubmissionEnvelope();
        envelope.notifyOfMetadataDocumentState(metadataDocument);
        getSubmissionEnvelopeRepository().save(envelope);
    }
}
