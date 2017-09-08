package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
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
public class Project extends MetadataDocument {
    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Project() {
        super(EntityType.PROJECT, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null);
        this.submissionEnvelope = null;
    }

    public Project(EntityType type,
                   Uuid uuid,
                   SubmissionDate submissionDate,
                   UpdateDate updateDate,
                   Accession accession,
                   SubmissionEnvelope submissionEnvelope,
                   Object content) {
        super(type, uuid, submissionDate, updateDate, accession, content);
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    public Project(Object content) {
        this(EntityType.PROJECT, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, content);
    }

    public Project addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }
}