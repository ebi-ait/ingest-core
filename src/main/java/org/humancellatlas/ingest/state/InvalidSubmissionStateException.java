package org.humancellatlas.ingest.state;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 10/09/17
 */
public class InvalidSubmissionStateException extends RuntimeException {
    public InvalidSubmissionStateException() {
        super();
    }

    public InvalidSubmissionStateException(String message) {
        super(message);
    }

    public InvalidSubmissionStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSubmissionStateException(Throwable cause) {
        super(cause);
    }

    protected InvalidSubmissionStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
