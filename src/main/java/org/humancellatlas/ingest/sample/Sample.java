package org.humancellatlas.ingest.sample;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
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
public class Sample extends BioMetadataDocument {
    private final @DBRef List<Sample> derivedFromSamples;
    private final @DBRef List<Project> projects;
    private final @DBRef List<Protocol> protocols;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Sample() {
        super(EntityType.SAMPLE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING);
        this.derivedFromSamples = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.protocols = new ArrayList<>();
        this.submissionEnvelope = null;
    }

    public Sample(EntityType type,
                  Uuid uuid,
                  SubmissionDate submissionDate,
                  UpdateDate updateDate,
                  Accession accession,
                  List<Sample> derivedFromSamples,
                  List<Project> projects,
                  List<Protocol> protocols,
                  SubmissionEnvelope submissionEnvelope,
                  Object content,
                  ValidationStatus validationStatus) {
        super(type, uuid, submissionDate, updateDate, accession, content, validationStatus);
        this.submissionEnvelope = submissionEnvelope;
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
             null,
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             null,
             content,
             ValidationStatus.PENDING);
    }

    public Sample addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }
}
