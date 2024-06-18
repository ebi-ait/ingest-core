package org.humancellatlas.ingest.core.exception;

public class RedundantUpdateException extends RuntimeException {
  public RedundantUpdateException() {}

  public RedundantUpdateException(String message) {
    super(message);
  }
}
