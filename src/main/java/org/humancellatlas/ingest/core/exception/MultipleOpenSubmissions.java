package org.humancellatlas.ingest.core.exception;

public class MultipleOpenSubmissions extends RuntimeException {

    public MultipleOpenSubmissions() {
        super();
    }

    public MultipleOpenSubmissions(String message) {
        super(message);
    }

    public MultipleOpenSubmissions(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleOpenSubmissions(Throwable cause) {
        super(cause);
    }

    protected MultipleOpenSubmissions(String message,
                                      Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
