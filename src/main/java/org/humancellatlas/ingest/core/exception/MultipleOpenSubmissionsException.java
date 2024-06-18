package org.humancellatlas.ingest.core.exception;

public class MultipleOpenSubmissionsException extends RuntimeException {

  public MultipleOpenSubmissionsException() {
    super();
  }

  public MultipleOpenSubmissionsException(String message) {
    super(message);
  }

  public MultipleOpenSubmissionsException(String message, Throwable cause) {
    super(message, cause);
  }

  public MultipleOpenSubmissionsException(Throwable cause) {
    super(cause);
  }

  protected MultipleOpenSubmissionsException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
