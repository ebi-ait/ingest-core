package org.humancellatlas.ingest.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.hateoas.Identifiable;
import org.zalando.problem.Problem;

import java.util.UUID;

@Data
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
public class SubmissionError extends IngestError implements Identifiable {

    @JsonIgnore @DBRef(lazy = true) private SubmissionEnvelope submissionEnvelope;
    @Id private String id;

    SubmissionError(SubmissionEnvelope submissionEnvelope, Problem submissionProblem) {
        super(submissionProblem);
        this.id = UUID.randomUUID().toString();
        this.submissionEnvelope = submissionEnvelope;
    }
}
