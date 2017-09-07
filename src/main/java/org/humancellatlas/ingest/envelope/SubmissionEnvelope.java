package org.humancellatlas.ingest.envelope;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.file.File;
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
    private final @DBRef List<Project> projects;
    private final @DBRef List<Sample> samples;
    private final @DBRef List<Assay> assays;
    private final @DBRef List<Analysis> analyses;
    private final @DBRef List<Protocol> protocols;
    private final @DBRef List<File> files;

    private @Setter SubmissionStatus submissionStatus;


    public SubmissionEnvelope(Uuid uuid,
                              SubmissionDate submissionDate,
                              UpdateDate updateDate,
                              SubmissionStatus submissionStatus,
                              List<Project> projects,
                              List<Sample> samples,
                              List<Assay> assays,
                              List<Analysis> analyses,
                              List<Protocol> protocols,
                              List<File> files) {
        super(EntityType.SUBMISSION, uuid, submissionDate, updateDate);
        this.submissionStatus = submissionStatus;
        this.projects = projects;
        this.samples = samples;
        this.assays = assays;
        this.analyses = analyses;
        this.protocols = protocols;
        this.files = files;
    }

    public SubmissionEnvelope() {
        this(null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             SubmissionStatus.DRAFT,
             new ArrayList<>(),
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

    public SubmissionEnvelope addFile(File file) {
        this.files.add(file);

        return this;
    }
}
