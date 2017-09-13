package org.humancellatlas.ingest.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Analysis extends MetadataDocument {
    private final @DBRef List<Project> projects = new ArrayList<>();
    private final @DBRef List<Assay> assays = new ArrayList<>();
    private final @DBRef List<File> files = new ArrayList<>();
    private final @DBRef List<BundleManifest> inputBundleManifests = new ArrayList<>();

    @JsonCreator
    public Analysis(Object content) {
        super(EntityType.ANALYSIS, content);
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
