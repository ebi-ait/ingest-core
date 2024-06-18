package org.humancellatlas.ingest.submission.exception;

import org.humancellatlas.ingest.security.exception.NotAllowedException;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

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
