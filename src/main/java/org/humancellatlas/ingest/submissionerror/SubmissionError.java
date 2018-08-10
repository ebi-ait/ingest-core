package org.humancellatlas.ingest.submissionerror;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.hateoas.Identifiable;

@AllArgsConstructor
@Getter
public class SubmissionError implements Identifiable<String> {
    private @Id
    @JsonIgnore
    String id;

    private final ErrorType errorType;
    private final String errorCode;
    private final String message;
    private final String details;

    @Setter
    private @DBRef
    SubmissionEnvelope submissionEnvelope;

}
