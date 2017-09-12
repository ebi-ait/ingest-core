package org.humancellatlas.ingest.core;

import lombok.Getter;
import org.humancellatlas.ingest.state.InvalidMetadataDocumentStateException;
import org.humancellatlas.ingest.state.ValidationState;
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
    private final List<Event> events;
    private final Accession accession;
    private final Object content;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    private ValidationState validationState;

    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

    protected MetadataDocument(EntityType type,
                               Uuid uuid,
                               SubmissionDate submissionDate,
                               UpdateDate updateDate,
                               List<Event> events,
                               Accession accession,
                               ValidationState validationState,
                               SubmissionEnvelope submissionEnvelope,
                               Object content) {
        super(type, uuid, submissionDate, updateDate);
        this.events = events;
        this.accession = accession;
        this.validationState = validationState;
        this.submissionEnvelope = submissionEnvelope;

        this.content = content;
    }

    public MetadataDocument addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }

    public boolean isInEnvelope(SubmissionEnvelope submissionEnvelope) {
        return this.submissionEnvelope.equals(submissionEnvelope);
    }

    public boolean isInEnvelopeWithUuid(Uuid uuid) {
        return this.submissionEnvelope.getUuid().equals(uuid);
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
                break;
            case INVALID:
                allowedStates.add(ValidationState.DRAFT);
                break;
            case PROCESSING:
                allowedStates.add(ValidationState.COMPLETE);
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
}
