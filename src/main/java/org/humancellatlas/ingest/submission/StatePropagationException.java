package org.humancellatlas.ingest.submission;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 28/09/17
 */
public class StatePropagationException extends RuntimeException {
    public StatePropagationException() {
        super();
    }

    public StatePropagationException(String message) {
        super(message);
    }

    public StatePropagationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatePropagationException(Throwable cause) {
        super(cause);
    }

    protected StatePropagationException(String message,
                                        Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
