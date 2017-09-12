package org.humancellatlas.ingest.project;

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
public class Project extends MetadataDocument {
    protected Project() {
        super(EntityType.PROJECT,
              null,
              new SubmissionDate(new Date()),
              new UpdateDate(new Date()),
              new ArrayList<>(),
              null,
              ValidationState.DRAFT,
              null,
              null);
    }

    public Project(EntityType type,
                   Uuid uuid,
                   SubmissionDate submissionDate,
                   UpdateDate updateDate,
                   List<Event> events,
                   Accession accession,
                   ValidationState validationState,
                   SubmissionEnvelope submissionEnvelope,
                   Object content) {
        super(type, uuid, submissionDate, updateDate, events, accession, validationState, submissionEnvelope, content);
    }

    @JsonCreator
    public Project(Object content) {
        this(EntityType.PROJECT,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             new ArrayList<>(),
             null,
             ValidationState.DRAFT, null,
             null);
    }

    public Project addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }
}