package uk.ac.ebi.subs.ingest.project.exception;

import uk.ac.ebi.subs.ingest.security.exception.NotAllowedException;

public class NotAllowedWithSubmissionInStateException extends NotAllowedException {
  public NotAllowedWithSubmissionInStateException() {
    super("Operation not allowed while the project has a submission in a non-editable state.");
  }
}
