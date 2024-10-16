package uk.ac.ebi.subs.ingest.notifications.sources;

import java.util.List;
import java.util.stream.Stream;

import uk.ac.ebi.subs.ingest.notifications.model.Notification;

public interface NotificationSource {

  Stream<Notification> stream();

  void supply(List<Notification> notifications);
}
