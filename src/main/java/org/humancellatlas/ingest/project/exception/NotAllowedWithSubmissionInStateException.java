package org.humancellatlas.ingest.project.exception;

import org.humancellatlas.ingest.security.exception.NotAllowedException;

public class NotAllowedWithSubmissionInStateException extends NotAllowedException {
    public NotAllowedWithSubmissionInStateException() {
        super("Operation not allowed while the project has a submission in a non-editable state.");
    }
}
