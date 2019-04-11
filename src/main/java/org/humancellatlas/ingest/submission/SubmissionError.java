package org.humancellatlas.ingest.submission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SubmissionError {
    private final ErrorType errorType;
    private final String errorCode;
    private final String message;
    private final String details;
}
