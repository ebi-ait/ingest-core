package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.validation.constraints.NotNull;
import java.time.Instant;
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
@EqualsAndHashCode(callSuper = true, exclude = {"supplementaryFiles", "submissionEnvelopes"})
public class Project extends MetadataDocument {
    @RestResource
    @JsonIgnore
    @DBRef(lazy = true)
    private Set<File> supplementaryFiles = new HashSet<>();

    // A project may have 1 or more submissions related to it.
    @JsonIgnore
    private @DBRef(lazy = true)
    Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    @Setter
    private Instant releaseDate;

    @Setter
    private Instant accessionDate;

    @Setter
    private Object technology;

    @Setter
    private Object organ;

    @Setter
    private Integer cellCount;

    @Setter
    private Object dataAccess;

    @Setter
    private Object identifyingOrganisms;

    @Setter
    private String primaryWrangler;
    
    @Setter
    private String secondaryWrangler;
    
    @Setter
    private WranglingState wranglingState;

    @Setter
    private Integer wranglingPriority;

    @Setter
    private String wranglingNotes;

    @Setter
    private Boolean isInCatalogue;

    @Setter
    private Instant cataloguedDate;

    @Setter
    private List<Object> publicationsInfo;

    @Setter
    private Integer dcpReleaseNumber;

    @Setter
    private List<String> projectLabels;

    @Setter
    private List<String> projectNetworks;


    @JsonCreator
    public Project(@JsonProperty("content") Object content) {
        super(EntityType.PROJECT, content);
    }

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

}
