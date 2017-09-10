package org.humancellatlas.ingest.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.project.Project;
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
public class Analysis extends BioMetadataDocument {
    private final @DBRef List<Project> projects;
    private final @DBRef List<Assay> assays;
    private final @DBRef List<File> files;
    private final @DBRef List<BundleManifest> inputBundleManifests;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected Analysis() {
        super(EntityType.ANALYSIS, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING, new ValidationChecksum());
        this.projects = new ArrayList<>();
        this.assays = new ArrayList<>();
        this.files = new ArrayList<>();
        this.inputBundleManifests = new ArrayList<>();
        this.submissionEnvelope = null;
    }

    public Analysis(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    Accession accession,
                    List<Project> projects,
                    List<Assay> assays,
                    List<File> files,
                    List<BundleManifest> inputBundleManifests,
                    SubmissionEnvelope submissionEnvelope,
                    Object content,
                    ValidationStatus validationStatus,
                    ValidationChecksum validationChecksum) {
        super(type, uuid, submissionDate, updateDate, accession, content, validationStatus, validationChecksum);
        this.projects = projects;
        this.assays = assays;
        this.files = files;
        this.inputBundleManifests = inputBundleManifests;
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    public Analysis(Object content) {
        this(EntityType.ANALYSIS,
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

    public Analysis addToEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }

    public Analysis addFile(File file) {
        this.files.add(file);

        return this;
    }
}
