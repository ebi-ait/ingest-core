package org.humancellatlas.ingest.core;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett

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
