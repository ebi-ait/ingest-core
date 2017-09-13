package org.humancellatlas.ingest.assay;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.sample.Sample;
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
public class Assay extends MetadataDocument {
    private final @DBRef List<Sample> samples = new ArrayList<>();
    private final @DBRef List<Project> projects = new ArrayList<>();
    private final @DBRef List<Protocol> protocols = new ArrayList<>();
    private final @DBRef List<File> files = new ArrayList<>();

    @JsonCreator
    public Assay(Object content) {
        super(EntityType.ASSAY, content);
    }

    public Assay addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }

    public Assay addFile(File file) {
        this.files.add(file);

        return this;
    }
}