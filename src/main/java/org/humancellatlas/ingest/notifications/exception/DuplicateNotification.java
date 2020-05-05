package org.humancellatlas.ingest.notifications.exception;

public class DuplicateNotification extends RuntimeException {
  public DuplicateNotification(String message) {
    super(message);
  }
}
