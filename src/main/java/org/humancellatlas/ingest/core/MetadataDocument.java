package org.humancellatlas.ingest.core;

import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
public abstract class MetadataDocument extends AbstractEntity {
    private final Accession accession;
    private final Object content;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    private @Setter ValidationState validationState;

    protected MetadataDocument(EntityType type,
                               Uuid uuid,
                               SubmissionDate submissionDate,
                               UpdateDate updateDate,
                               Accession accession,
                               ValidationState validationState,
                               SubmissionEnvelope submissionEnvelope,
                               Object content) {
        super(type, uuid, submissionDate, updateDate);

        this.accession = accession;
        this.validationState = validationState;
        this.submissionEnvelope = submissionEnvelope;

        this.content = content;
    }

    public MetadataDocument addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }

    public boolean isInEnvelope(SubmissionEnvelope submissionEnvelope) {
        return this.submissionEnvelope.equals(submissionEnvelope);
    }

    public boolean isInEnvelopeWithUuid(Uuid uuid) {
        return this.submissionEnvelope.getUuid().equals(uuid);
    }
}
