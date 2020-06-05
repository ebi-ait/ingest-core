package org.humancellatlas.ingest.notifications.sources.impl.inmemory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;

public class InmemoryNotificationSource implements NotificationSource {
  private final Queue<Notification> queue = new ConcurrentLinkedQueue<>();

  @Override
  public Stream<Notification> stream() {
    return Stream.generate(() -> {
      try{
        return queue.remove();
      } catch(NoSuchElementException e) {
        return null;
      }
    }).takeWhile(Objects::nonNull);
  }

  @Override
  public void supply(List<Notification> notifications) {
    notifications.forEach(this.queue::add);
  }
}
