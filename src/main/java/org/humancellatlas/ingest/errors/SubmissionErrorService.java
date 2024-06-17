package org.humancellatlas.ingest.errors;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

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
