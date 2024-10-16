package uk.ac.ebi.subs.ingest.notifications.exception;

public class ProcessingException extends RuntimeException {
  public ProcessingException(String message) {
    super(message);
  }

  public ProcessingException(Throwable e) {
    super(e);
  }
}
