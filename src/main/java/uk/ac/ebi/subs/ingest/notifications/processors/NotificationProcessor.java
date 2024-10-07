package uk.ac.ebi.subs.ingest.notifications.processors;

import uk.ac.ebi.subs.ingest.notifications.model.Notification;

public interface NotificationProcessor {

  boolean isEligible(Notification notification);

  void handle(Notification notification);
}
