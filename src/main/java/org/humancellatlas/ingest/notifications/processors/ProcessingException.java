package org.humancellatlas.ingest.notifications.processors;

public class ProcessingException extends RuntimeException {
  public ProcessingException(String message) {
    super(message);
  }
}
