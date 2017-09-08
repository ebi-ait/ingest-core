package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Protocol extends BioMetadataDocument {
    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Protocol() {
        super(EntityType.PROTOCOL, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING);
        this.submissionEnvelope = null;
    }

    public Protocol(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    Accession accession,
                    SubmissionEnvelope submissionEnvelope,
                    Object content,
                    ValidationStatus validationStatus) {
        super(type, uuid, submissionDate, updateDate, accession, content, validationStatus);
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    public Protocol(Object content) {
        this(EntityType.PROTOCOL,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             null,
             null,
             content,
             ValidationStatus.PENDING);
    }

    public Protocol addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }
}
