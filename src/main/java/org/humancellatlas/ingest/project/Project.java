package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.LinkToNewSubmissionNotAllowedException;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.HashSet;
import java.util.Set;

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Project(Object content) {
        super(EntityType.PROJECT, content);
    }

    // A project may have 1 or more submissions.
    private @DBRef(lazy = true) Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    // This method is moved from MetadataDocument to Project class during the subEnvs refactoring
    // It makes sense to add subEnv to project, not all other entity types.
    public MetadataDocument addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        SubmissionEnvelope openSubmission = this.getOpenSubmissionEnvelope();
        if( openSubmission == null ){
            if(this.getValidationState() != ValidationState.DRAFT){
                this.enactStateTransition(ValidationState.DRAFT);
            }
            this.submissionEnvelopes.add(submissionEnvelope);
        }
        else if (!openSubmission.getId().equals(submissionEnvelope.getId())){
            String errorMessage = String.format("The %s metadata %s is still linked to a %s submission envelope %s.",
                    this.getType(), this.getId(), openSubmission.getSubmissionState(), openSubmission.getId());
            getLog().error(errorMessage);

            throw new LinkToNewSubmissionNotAllowedException(errorMessage);
        }
        return this;
    }

    @JsonIgnore
    public SubmissionEnvelope getOpenSubmissionEnvelope(){
        for (SubmissionEnvelope submissionEnvelope : this.submissionEnvelopes) {
            if (submissionEnvelope.isOpen()){
                return submissionEnvelope;
            }
        }
        return null;
    }

    @JsonIgnore
    public void removeSubmissionEnvelopeData(SubmissionEnvelope submissionEnvelope, boolean forceRemoval) {
        if(!submissionEnvelopes.contains(submissionEnvelope))
            throw new UnsupportedOperationException(
                    String.format("Submission Envelope (%s) is not part of Project (%s), so it cannot be removed.",
                            submissionEnvelope.getUuid().getUuid().toString(),
                            this.getUuid().getUuid().toString()
                    ));
        if(!(submissionEnvelope.isOpen() || forceRemoval))
            throw new UnsupportedOperationException("Cannot remove submission from Project since it is already submitted!");

        this.supplementaryFiles.removeIf(file -> file.getSubmissionEnvelope().equals(submissionEnvelope));
        submissionEnvelopes.remove(submissionEnvelope);
    }
}
