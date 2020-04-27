package org.humancellatlas.ingest.notifications.processors;

import org.humancellatlas.ingest.notifications.model.Notification;

public interface NotificationProcessor {

  boolean isEligible(Notification notification);

  void handle(Notification notification);
}
