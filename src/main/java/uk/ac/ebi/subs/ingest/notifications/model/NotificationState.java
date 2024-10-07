package uk.ac.ebi.subs.ingest.notifications.model;

import java.util.List;

public enum NotificationState {
  PENDING,
  QUEUED,
  PROCESSING,
  PROCESSED,
  FAILED;

  public List<NotificationState> legalTransitions() {
    switch (this) {
      case PENDING:
        return List.of(QUEUED, FAILED);
      case QUEUED:
        return List.of(PROCESSING, FAILED);
      case PROCESSING:
        return List.of(PROCESSED, FAILED);
      case PROCESSED:
        return List.of(FAILED);
      case FAILED:
        return List.of(QUEUED, FAILED);
      default:
        throw new IllegalStateException(
            String.format("Unknown state transitions from notification state %s", this));
    }
  }

  public boolean isLegalTransition(NotificationState toState) {
    return this.legalTransitions().contains(toState);
  }
}
