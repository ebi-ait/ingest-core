package org.humancellatlas.ingest.submission;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@RepositoryEventHandler
@RequiredArgsConstructor
@Getter
public class SubmissionEnvelopeStateChangeHandler {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @HandleAfterCreate
    public void handleMetadataDocumentCreate(MetadataDocument metadataDocument) {
        SubmissionEnvelope envelope = metadataDocument.getSubmissionEnvelope();
        envelope.notifyOfMetadataDocumentState(metadataDocument);
        getSubmissionEnvelopeRepository().save(envelope);
    }

    @HandleAfterSave
    public void handleMetadataDocumentSave(MetadataDocument metadataDocument) {
        SubmissionEnvelope envelope = metadataDocument.getSubmissionEnvelope();
        envelope.notifyOfMetadataDocumentState(metadataDocument);
        getSubmissionEnvelopeRepository().save(envelope);
    }
}
