package org.humancellatlas.ingest.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.ValidationState;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.project.Project;
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
public class Analysis extends MetadataDocument {
    private final @DBRef List<Project> projects;
    private final @DBRef List<Assay> assays;
    private final @DBRef List<File> files;
    private final @DBRef List<BundleManifest> inputBundleManifests;

    protected Analysis() {
        super(EntityType.ANALYSIS,
              null,
              new SubmissionDate(new Date()),
              new UpdateDate(new Date()),
              null,
              ValidationState.PENDING,
              null,
              null);
        this.projects = new ArrayList<>();
        this.assays = new ArrayList<>();
        this.files = new ArrayList<>();
        this.inputBundleManifests = new ArrayList<>();
    }

    public Analysis(EntityType type,
                    Uuid uuid,
                    SubmissionDate submissionDate,
                    UpdateDate updateDate,
                    Accession accession,
                    ValidationState validationState,
                    List<Project> projects,
                    List<Assay> assays,
                    List<File> files,
                    List<BundleManifest> inputBundleManifests,
                    SubmissionEnvelope submissionEnvelope,
                    Object content) {
        super(type, uuid, submissionDate, updateDate, accession, validationState, submissionEnvelope, content);
        this.projects = projects;
        this.assays = assays;
        this.files = files;
        this.inputBundleManifests = inputBundleManifests;
    }

    @JsonCreator
    public Analysis(Object content) {
        this(EntityType.ANALYSIS,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             null,
             null,
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             new ArrayList<>(),
             null,
             content
        );
    }

    public Analysis addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }

    public Analysis addFile(File file) {
        this.files.add(file);

        return this;
    }
}
