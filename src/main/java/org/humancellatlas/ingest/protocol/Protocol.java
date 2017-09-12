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

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Protocol extends MetadataDocument {
    protected Protocol() {
        super(EntityType.PROTOCOL,
              null,
              new SubmissionDate(new Date()),
              new UpdateDate(new Date()),
              null,
              ValidationState.PENDING,
              null,
              null);
    }

    public Protocol(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    Accession accession,
                    ValidationState validationState, SubmissionEnvelope submissionEnvelope,
                    Object content) {
        super(type, uuid, submissionDate, updateDate, accession, validationState, submissionEnvelope, content);
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
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }
}
