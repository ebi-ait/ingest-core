package org.humancellatlas.ingest.notifications.sources;

import java.util.List;
import java.util.stream.Stream;
import org.humancellatlas.ingest.notifications.model.Notification;

public interface NotificationSource {

    Stream<Notification> stream();

    void supply(List<Notification> notifications);
}