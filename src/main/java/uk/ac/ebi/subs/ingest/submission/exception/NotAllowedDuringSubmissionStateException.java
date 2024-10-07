package uk.ac.ebi.subs.ingest.submission.exception;

import uk.ac.ebi.subs.ingest.security.exception.NotAllowedException;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

public class NotAllowedDuringSubmissionStateException extends NotAllowedException {
  public NotAllowedDuringSubmissionStateException() {
    super("Operation not allowed during the current submission state for the envelope.");
  }

  public NotAllowedDuringSubmissionStateException(SubmissionEnvelope submissionEnvelope) {
    super(
        String.format(
            "Operation not allowed during the current submission state %s for the envelope %s",
            submissionEnvelope.getSubmissionState(), submissionEnvelope.getUuid()));
  }
}
