package org.humancellatlas.ingest.core;

import lombok.Getter;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Getter
public class ValidationEvent extends ApplicationEvent {
    private String message;

    public ValidationEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
