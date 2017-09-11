package org.humancellatlas.ingest.assay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
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
public class Assay extends BioMetadataDocument {
    private final @DBRef List<Sample> samples;
    private final @DBRef List<Project> projects;
    private final @DBRef List<Protocol> protocols;
    private final @DBRef List<File> files;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Assay() {
        super(EntityType.ASSAY, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING, new ValidationChecksum());
        this.samples = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.protocols = new ArrayList<>();
        this.files = new ArrayList<>();
        this.submissionEnvelope = null;
    }

    public Assay(EntityType type,
                 Uuid uuid,
                 SubmissionDate submissionDate,
                 UpdateDate updateDate,
                 Accession accession,
                 List<Sample> samples,
                 List<Project> projects,
                 List<Protocol> protocols,
                 List<File> files,
                 SubmissionEnvelope submissionEnvelope,
                 Object content,
                 ValidationStatus validationStatus,
                 ValidationChecksum validationChecksum) {
        super(type, uuid, submissionDate, updateDate, accession, content, validationStatus, validationChecksum);
        this.samples = samples;
        this.projects = projects;
        this.protocols = protocols;
        this.files = files;
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    public Assay(Object content) {
        this(EntityType.ASSAY,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             null,
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             null,
             content,
             ValidationStatus.PENDING,
             new ValidationChecksum());
    }

    public Assay addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        // mark this submission envelope as having received new metadata
        this.submissionEnvelope.markDraft();

        return this;
    }

    public Assay addFile(File file) {
        this.files.add(file);

        return this;
    }
}