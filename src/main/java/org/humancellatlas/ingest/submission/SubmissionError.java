package org.humancellatlas.ingest.submission;

import lombok.Data;

@Data
public class SubmissionError {
    private ErrorType errorType;
    private String errorCode;
    private String message;
    private String details;
}
