package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
              new ArrayList<>(), null,
              ValidationState.DRAFT,
              null,
              null);
    }

    public Protocol(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    List<Event> events,
                    Accession accession,
                    ValidationState validationState, SubmissionEnvelope submissionEnvelope,
                    Object content) {
        super(type, uuid, submissionDate, updateDate, events, accession, validationState, submissionEnvelope, content);
    }

    @JsonCreator
    public Protocol(Object content) {
        this(EntityType.PROTOCOL,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             new ArrayList<>(),
             null,
             ValidationState.DRAFT, null,
             content
        );
    }

    public Protocol addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }
}
