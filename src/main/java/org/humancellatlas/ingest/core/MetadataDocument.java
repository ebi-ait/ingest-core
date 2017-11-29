package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.exception.LinkToNewSubmissionNotAllowedException;
import org.humancellatlas.ingest.state.InvalidMetadataDocumentStateException;
import org.humancellatlas.ingest.state.InvalidSubmissionStateException;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
public abstract class MetadataDocument extends AbstractEntity {
    private final List<Event> events = new ArrayList<>();
    private final Object content;

    private final @DBRef List<SubmissionEnvelope> submissionEnvelopes = new ArrayList<>();

    private @Setter Accession accession;
    private @Setter ValidationState validationState;
    private @Setter List<Object> validationErrors;

    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

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
        else{
            String errorMessage = String.format("The %s metadata %s is still linked to a %s submission envelope %s.",
                    this.getType(), this.getId(), openSubmission.getSubmissionState(), openSubmission.getId());
            getLog().error(errorMessage);

            throw new LinkToNewSubmissionNotAllowedException(errorMessage);
        }
        return this;
    }

    public boolean isInEnvelope(SubmissionEnvelope submissionEnvelope) {
        return this.getOpenSubmissionEnvelope().equals(submissionEnvelope);
    }

    public boolean isInEnvelopeWithUuid(Uuid uuid) {
        return this.getOpenSubmissionEnvelope().getUuid().equals(uuid);
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

    public MetadataDocument addEvent(Event event) {
        this.events.add(event);

        return this;
    }

    public MetadataDocument enactStateTransition(ValidationState targetState) {
        if (!allowedStateTransitions().contains(targetState)) {
            throw new InvalidMetadataDocumentStateException(
                    String.format("The validation state '%s' is not recognised as a state that can be set",
                                  targetState.name()));
        }
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
