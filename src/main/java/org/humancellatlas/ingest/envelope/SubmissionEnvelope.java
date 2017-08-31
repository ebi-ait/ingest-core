package org.humancellatlas.ingest.envelope;

import lombok.Getter;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.sample.Sample;
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
public class SubmissionEnvelope extends AbstractEntity {
    @DBRef
    private List<Project> projects;
    @DBRef
    private List<Sample> samples;
    @DBRef
    private List<Assay> assays;
    @DBRef
    private List<Analysis> analyses;
    @DBRef
    private List<Protocol> protocols;

    public SubmissionEnvelope(Uuid uuid,
                              SubmissionDate submissionDate,
                              UpdateDate updateDate,
                              List<Project> projects,
                              List<Sample> samples,
                              List<Assay> assays,
                              List<Analysis> analyses,
                              List<Protocol> protocols) {
        super(EntityType.SUBMISSION, uuid, submissionDate, updateDate);
        this.projects = projects;
        this.samples = samples;
        this.assays = assays;
        this.analyses = analyses;
        this.protocols = protocols;
    }

    public SubmissionEnvelope() {
        this(null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>());
    }
}
