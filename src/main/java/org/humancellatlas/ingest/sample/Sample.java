package org.humancellatlas.ingest.sample;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
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
public class Sample extends MetadataDocument {
    private final @DBRef List<Sample> derivedFromSamples = new ArrayList<>();
    private final @DBRef List<Project> projects = new ArrayList<>();
    private final @DBRef List<Protocol> protocols = new ArrayList<>();

    @JsonCreator
    public Sample(Object content) {
        super(EntityType.SAMPLE, content);
    }

    public Sample addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }
}
