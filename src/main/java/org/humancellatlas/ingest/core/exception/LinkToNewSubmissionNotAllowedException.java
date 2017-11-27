package org.humancellatlas.ingest.core.exception;

public class LinkToNewSubmissionNotAllowedException extends RuntimeException {

    public LinkToNewSubmissionNotAllowedException() {
        super();
    }

    public LinkToNewSubmissionNotAllowedException(String message) {
        super(message);
    }

    public LinkToNewSubmissionNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinkToNewSubmissionNotAllowedException(Throwable cause) {
        super(cause);
    }

    protected LinkToNewSubmissionNotAllowedException(String message,
                                             Throwable cause,
                                             boolean enableSuppression,
                                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
