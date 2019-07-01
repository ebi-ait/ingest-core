package org.humancellatlas.ingest.submission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class SubmissionError {
    private ErrorType errorType;
    private String errorCode;
    private String message;
    private String details;
}
