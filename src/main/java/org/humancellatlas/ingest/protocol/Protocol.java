package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Protocol extends MetadataDocument {
    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Protocol() {
        super(EntityType.PROTOCOL,
              null,
              new SubmissionDate(new Date()),
              new UpdateDate(new Date()),
              null,
              ValidationState.PENDING,
              null);
        this.submissionEnvelope = null;
    }

    public Protocol(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    Accession accession,
                    ValidationState validationState, SubmissionEnvelope submissionEnvelope,
                    Object content) {
        super(type, uuid, submissionDate, updateDate, accession, validationState, content);
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    public Protocol(Object content) {
        this(EntityType.PROTOCOL,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             null,
             ValidationState.PENDING, null,
             content
        );
    }

    public Protocol addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }
}
