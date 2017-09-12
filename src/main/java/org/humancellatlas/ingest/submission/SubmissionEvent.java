package org.humancellatlas.ingest.submission;

import lombok.Getter;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.InvalidSubmissionStateException;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
public class SubmissionEvent extends Event {
    private final SubmissionState originalState;
    private final SubmissionState endState;

    public SubmissionEvent(SubmissionState originalState, SubmissionState endState) {
        super(new SubmissionDate(new Date()));

        // check if this is a valid state transition
        if (!SubmissionEnvelope.allowedStateTransitions(originalState).contains(endState)) {
            throw new InvalidSubmissionStateException(String.format("'%s' is not an allowed state transition from " +
                                                                            "'%s'", endState.name(), originalState.name()));
        }

        this.originalState = originalState;
        this.endState = endState;
    }
}
