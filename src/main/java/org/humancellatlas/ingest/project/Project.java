package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Project extends MetadataDocument {
    @RestResource
    @DBRef(lazy = true)
    private Set<File> supplementaryFiles = new HashSet<>();

    // A project may have 1 or more submissions related to it.
    private @DBRef(lazy = true)
    Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Project(Object content) {
        super(EntityType.PROJECT, content);
    }

    public void addToSubmissionEnvelopes(@NotNull SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelopes.add(submissionEnvelope);
    }

    @JsonIgnore
    public List<SubmissionEnvelope> getOpenSubmissionEnvelopes(){
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .filter(SubmissionEnvelope::isOpen)
                .collect(Collectors.toList());
    }

    public Boolean getHasOpenSubmission(){
        return !getOpenSubmissionEnvelopes().isEmpty();
    }

    @JsonIgnore
    public void removeSubmissionEnvelopeData(@NotNull SubmissionEnvelope submissionEnvelope, boolean forceRemoval) {
        if (!submissionEnvelopes.contains(submissionEnvelope))
            throw new UnsupportedOperationException(
                    String.format("Submission Envelope (%s) is not part of Project (%s), so it cannot be removed.",
                            submissionEnvelope.getUuid().getUuid().toString(),
                            this.getUuid().getUuid().toString()
                    ));
        if (!(submissionEnvelope.isOpen() || forceRemoval))
            throw new UnsupportedOperationException("Cannot remove submission from Project since it is already submitted!");

        submissionEnvelopes.remove(submissionEnvelope);
    }
}
