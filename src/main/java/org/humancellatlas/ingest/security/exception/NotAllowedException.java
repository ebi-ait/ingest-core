package org.humancellatlas.ingest.security.exception;

public class NotAllowedException extends RuntimeException {
  public NotAllowedException() {
    super("Operation not allowed.");
  }

  public NotAllowedException(String customMessage) {
    super(customMessage);
  }
}
