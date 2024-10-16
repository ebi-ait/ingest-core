package uk.ac.ebi.subs.ingest.notifications.exception;

public class DuplicateNotification extends RuntimeException {
  public DuplicateNotification(String message) {
    super(message);
  }
}
