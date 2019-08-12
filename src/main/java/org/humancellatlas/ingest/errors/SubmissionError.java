package org.humancellatlas.ingest.errors;

import lombok.Data;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
public class SubmissionError {
    @DBRef private SubmissionEnvelope submissionEnvelope;
    @Id private String id;
    private ErrorType errorType;
    private String errorCode;
    private String message;
    private String details;
}
