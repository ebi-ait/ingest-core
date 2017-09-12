package org.humancellatlas.ingest.sample;

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
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

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
public class Sample extends MetadataDocument {
    private final @DBRef List<Sample> derivedFromSamples;
    private final @DBRef List<Project> projects;
    private final @DBRef List<Protocol> protocols;

    protected Sample() {
        super(EntityType.SAMPLE,
              null,
              new SubmissionDate(new Date()),
              new UpdateDate(new Date()),
              new ArrayList<>(),
              null,
              ValidationState.DRAFT,
              null,
              null);
        this.derivedFromSamples = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.protocols = new ArrayList<>();
    }

    public Sample(EntityType type,
                  Uuid uuid,
                  SubmissionDate submissionDate,
                  UpdateDate updateDate,
                  List<Event> events,
                  Accession accession,
                  ValidationState validationState, List<Sample> derivedFromSamples,
                  List<Project> projects,
                  List<Protocol> protocols,
                  SubmissionEnvelope submissionEnvelope,
                  Object content) {
        super(type, uuid, submissionDate, updateDate, events, accession, validationState, submissionEnvelope, content);
        this.derivedFromSamples = derivedFromSamples;
        this.projects = projects;
        this.protocols = protocols;
    }

    @JsonCreator
    public Sample(Object content) {
        this(EntityType.SAMPLE,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             new ArrayList<>(),
             null,
             ValidationState.DRAFT, new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             null,
             content
        );
    }

    public Sample addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }
}
