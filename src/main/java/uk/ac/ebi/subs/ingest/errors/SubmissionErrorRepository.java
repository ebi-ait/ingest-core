package uk.ac.ebi.subs.ingest.errors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@CrossOrigin
public interface SubmissionErrorRepository extends MongoRepository<SubmissionError, String> {
  Page<SubmissionError> findBySubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Pageable pageable);

  @RestResource(exported = false)
  Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);
}
