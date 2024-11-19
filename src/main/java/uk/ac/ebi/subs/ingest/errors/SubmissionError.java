package uk.ac.ebi.subs.ingest.errors;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.hateoas.Identifiable;
import org.zalando.problem.Problem;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Data
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
public class SubmissionError extends IngestError implements Identifiable {

  @JsonIgnore
  @DBRef(lazy = true)
  private SubmissionEnvelope submissionEnvelope;

  @Id private String id;

  SubmissionError(SubmissionEnvelope submissionEnvelope, Problem submissionProblem) {
    super(submissionProblem);
    this.id = UUID.randomUUID().toString();
    this.submissionEnvelope = submissionEnvelope;
  }
}
