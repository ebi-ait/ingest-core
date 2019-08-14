package org.humancellatlas.ingest.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.zalando.problem.Problem;

import java.net.URI;
import java.util.UUID;

@Data
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
public class SubmissionError extends IngestError {
    @JsonIgnore @DBRef private SubmissionEnvelope submissionEnvelope;
    @Id private String id;

    SubmissionError(SubmissionEnvelope submissionEnvelope, Problem submissionProblem) {
        super(submissionProblem);
        this.id = UUID.randomUUID().toString();
        this.submissionEnvelope = submissionEnvelope;
    }

    @Override
    public URI getInstance() {
        URI base = URI.create("submissionErrors/");
        return base.resolve(getId());
    }
}
