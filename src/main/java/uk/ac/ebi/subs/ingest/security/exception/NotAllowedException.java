package uk.ac.ebi.subs.ingest.security.exception;

public class NotAllowedException extends RuntimeException {
  public NotAllowedException() {
    super("Operation not allowed.");
  }

  public NotAllowedException(String customMessage) {
    super(customMessage);
  }
}
