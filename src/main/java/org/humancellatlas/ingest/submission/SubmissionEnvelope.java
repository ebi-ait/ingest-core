package org.humancellatlas.ingest.submission;

import lombok.Getter;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.submission.state.InvalidSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class SubmissionEnvelope extends AbstractEntity {
    private final List<Event> events;

    private SubmissionState submissionState;

    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

    public SubmissionEnvelope(Uuid uuid,
                              SubmissionDate submissionDate,
                              UpdateDate updateDate,
                              SubmissionState submissionState) {
        super(EntityType.SUBMISSION, uuid, submissionDate, updateDate);

        this.events = new ArrayList<>();
        this.submissionState = submissionState;
    }

    public SubmissionEnvelope() {
        this(null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             SubmissionState.DRAFT);
    }

    public static List<SubmissionState> allowedStateTransitions(SubmissionState fromState) {
        List<SubmissionState> allowedStates = new ArrayList<>();
        switch (fromState) {
            case PENDING:
                allowedStates.add(SubmissionState.DRAFT);
                break;
            case DRAFT:
                allowedStates.add(SubmissionState.VALIDATING);
                break;
            case VALIDATING:
                allowedStates.add(SubmissionState.VALID);
                allowedStates.add(SubmissionState.INVALID);
                break;
            case VALID:
                allowedStates.add(SubmissionState.DRAFT);
                allowedStates.add(SubmissionState.SUBMITTED);
                break;
            case INVALID:
                allowedStates.add(SubmissionState.DRAFT);
                allowedStates.add(SubmissionState.VALIDATING);
                break;
            case SUBMITTED:
                allowedStates.add(SubmissionState.PROCESSING);
                break;
            case PROCESSING:
                allowedStates.add(SubmissionState.CLEANUP);
                break;
            case CLEANUP:
                allowedStates.add(SubmissionState.COMPLETE);
                break;
            default:
                getLog().warn(String.format("There are no legal state transitions for '%s' state", fromState.name()));
                break;
        }
        return allowedStates;
    }

    public List<SubmissionState> allowedStateTransitions() {
        return allowedStateTransitions(getSubmissionState());
    }

    public void markDraft() {
        enactStateTransition(SubmissionState.DRAFT);
    }

    public SubmissionEnvelope addEvent(Event event) {
        this.events.add(event);

        return this;
    }

    public SubmissionEnvelope enactStateTransition(SubmissionState targetState) {
        if (!allowedStateTransitions().contains(targetState)) {
            throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                    "as a submission envelope state that can be set", submissionState.name()));
        }
        this.submissionState = targetState;

        return this;
    }
}
