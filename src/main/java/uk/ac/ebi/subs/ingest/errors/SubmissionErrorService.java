package uk.ac.ebi.subs.ingest.errors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Service
public class SubmissionErrorService {
  @Autowired private SubmissionErrorRepository submissionErrorRepository;

  public Page<SubmissionError> getErrorsFromEnvelope(
      SubmissionEnvelope submissionEnvelope, Pageable pageable) {
    return submissionErrorRepository.findBySubmissionEnvelope(submissionEnvelope, pageable);
  }

  public SubmissionError addErrorToEnvelope(
      SubmissionEnvelope submissionEnvelope, Problem submissionProblem) {
    SubmissionError submissionError = new SubmissionError(submissionEnvelope, submissionProblem);
    submissionErrorRepository.insert(submissionError);
    return submissionError;
  }

  public void deleteSubmissionEnvelopeErrors(SubmissionEnvelope submissionEnvelope) {
    submissionErrorRepository.deleteBySubmissionEnvelope(submissionEnvelope);
  }
}
