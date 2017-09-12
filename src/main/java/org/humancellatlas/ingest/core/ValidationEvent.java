package org.humancellatlas.ingest.core;

import lombok.Getter;
import org.humancellatlas.ingest.state.ValidationState;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
public class ValidationEvent extends Event {
    private final ValidationState originalState;
    private final ValidationState endState;

    public ValidationEvent(ValidationState originalState, ValidationState endState) {
        super(new SubmissionDate(new Date()));
        this.originalState = originalState;
        this.endState = endState;
    }
}
