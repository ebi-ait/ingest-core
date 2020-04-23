package org.humancellatlas.ingest.notifications;

import java.util.Arrays;
import java.util.List;

public enum NotificationState {
  REGISTERED,
  QUEUED,
  PROCESSING,
  PROCESSED,
  FAILED;

  public List<NotificationState> legalTransitions() {
    if (this.equals(REGISTERED)) {
      return Arrays.asList(QUEUED, FAILED);
    } else if (this.equals(QUEUED)) {
      return Arrays.asList(PROCESSING, FAILED);
    } else if (this.equals(PROCESSING)) {
      return Arrays.asList(PROCESSED, FAILED);
    } else if (this.equals(FAILED)) {
      return Arrays.asList(QUEUED, FAILED);
    } else {
      throw new IllegalStateException(
          String.format("Unknown state transitions from notification state %s", this));
    }
  }
}
