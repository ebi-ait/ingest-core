package uk.ac.ebi.subs.ingest.notifications.sources.impl.inmemory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.sources.NotificationSource;

public class InmemoryNotificationSource implements NotificationSource {

  private final Queue<Notification> queue = new ConcurrentLinkedQueue<>();

  @Override
  public Stream<Notification> stream() {
    return Stream.generate(
            () -> {
              try {
                return queue.remove();
              } catch (NoSuchElementException e) {
                return null;
              }
            })
        .takeWhile(Objects::nonNull);
  }

  // ignore IDE suggestion to replace this.queue::add with addAll(); addAll isn't thread safe for
  // this particular queue implementation (ConcurrentLinkedQueue)
  @Override
  public void supply(List<Notification> notifications) {
    notifications.forEach(this.queue::add);
  }
}
