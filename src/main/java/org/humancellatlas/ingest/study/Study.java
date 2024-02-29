package org.humancellatlas.ingest.study;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@JsonIgnoreProperties({"firstDcpVersion", "dcpVersion", "validationState",
        "validationErrors", "graphValidationErrors", "isUpdate"})
public class Study extends MetadataDocument {

    // A study may have 1 or more submissions related to it.
    @JsonIgnore
    private @DBRef(lazy = true)
    Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    public void addToSubmissionEnvelopes(@NotNull SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelopes.add(submissionEnvelope);
    }

    //ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
    @JsonIgnore
    public List<SubmissionEnvelope> getOpenSubmissionEnvelopes() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .filter(env -> env.getSubmissionState() != null)
                .filter(SubmissionEnvelope::isOpen)
                .collect(Collectors.toList());
    }

    public Boolean getHasOpenSubmission() {
        return !getOpenSubmissionEnvelopes().isEmpty();
    }

    @JsonIgnore
    public Boolean isEditable() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .allMatch(SubmissionEnvelope::isEditable);
    }

    @JsonCreator
    public Study(@JsonProperty("content") Object content) {
        super(EntityType.STUDY, content);
    }

}
