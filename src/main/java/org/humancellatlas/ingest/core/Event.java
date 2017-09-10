package org.humancellatlas.ingest.core;

import lombok.Getter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionState;
import org.humancellatlas.ingest.submission.state.InvalidSubmissionStateException;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author tburdett
 * @date 10/09/2017
 */
@Getter
public class Event {
    private final SubmissionDate submissionDate;
    private final SubmissionState originalState;
    private final SubmissionState endState;

    public Event(SubmissionState originalState, SubmissionState endState) {
        // check if this is a valid state transition
        if (!SubmissionEnvelope.allowedStateTransitions(originalState).contains(endState)) {
            throw new InvalidSubmissionStateException(String.format("'%s' is not an allowed state transition from " +
                    "'%s'", endState.name(), originalState.name()));
        }

        this.submissionDate = new SubmissionDate(new Date());
        this.originalState = originalState;
        this.endState = endState;
    }
}
