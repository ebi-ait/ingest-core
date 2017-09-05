package org.humancellatlas.ingest.envelope;

import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.core.*;
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
    @DBRef(lazy = true)
    private List<Project> projects;
    @DBRef(lazy = true)
    private List<Sample> samples;
    @DBRef(lazy = true)
    private List<Assay> assays;
    @DBRef(lazy = true)
    private List<Analysis> analyses;
    @DBRef(lazy = true)
    private List<Protocol> protocols;

    @Setter
    private SubmissionStatus submissionStatus;

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
        this.submissionStatus = SubmissionStatus.DRAFT;
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

    public SubmissionEnvelope addAnalysis(Analysis analysis) {
        this.analyses.add(analysis);

        return this;
    }

    public SubmissionEnvelope addAssay(Assay assay) {
        this.assays.add(assay);

        return this;
    }

    public SubmissionEnvelope addProject(Project project) {
        this.projects.add(project);

        return this;
    }

    public SubmissionEnvelope addProtocol(Protocol protocol) {
        this.protocols.add(protocol);

        return this;
    }

    public SubmissionEnvelope addSample(Sample sample) {
        this.samples.add(sample);

        return this;
    }
}
