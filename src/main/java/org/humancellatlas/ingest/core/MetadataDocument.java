package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.exception.LinkToNewSubmissionNotAllowedException;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
public abstract class MetadataDocument extends AbstractEntity {

    @Setter
    private Instant dcpVersion;

    @Setter
    private Object content;

    @Indexed
    private @DBRef(lazy = true) Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

    private @Setter Accession accession;
    private @Setter ValidationState validationState;
    private @Setter List<Object> validationErrors;

    private @Setter @Field
    Boolean isUpdate = false;


    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

    protected MetadataDocument(){}

    protected MetadataDocument(EntityType type,
                               Object content) {
        super(type);
        this.content = content;
        this.validationState = ValidationState.DRAFT;
    }

    public MetadataDocument addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        SubmissionEnvelope openSubmission = this.getOpenSubmissionEnvelope();
        if( openSubmission == null ){
            if(this.validationState != ValidationState.DRAFT){
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

    public static List<ValidationState> allowedStateTransitions(ValidationState fromState) {
        List<ValidationState> allowedStates = new ArrayList<>();
        switch (fromState) {
            case DRAFT:
                allowedStates.add(ValidationState.VALIDATING);
                break;
            case VALIDATING:
                allowedStates.add(ValidationState.VALID);
                allowedStates.add(ValidationState.INVALID);
                break;
            case VALID:
                allowedStates.add(ValidationState.PROCESSING);
                allowedStates.add(ValidationState.DRAFT);
                break;
            case INVALID:
                allowedStates.add(ValidationState.DRAFT);
                break;
            case PROCESSING:
                allowedStates.add(ValidationState.COMPLETE);
                allowedStates.add(ValidationState.DRAFT);
                break;
            default:
                getLog().warn(String.format("There are no legal state transitions for '%s' state", fromState.name()));
                break;
        }
        return allowedStates;
    }

    public List<ValidationState> allowedStateTransitions() {
        return allowedStateTransitions(getValidationState());
    }


    public MetadataDocument enactStateTransition(ValidationState targetState) {
        this.validationState = targetState;

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
}
