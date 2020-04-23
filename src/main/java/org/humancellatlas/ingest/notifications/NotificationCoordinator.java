package org.humancellatlas.ingest.notifications;

import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NotificationCoordinator {

  private final @NonNull NotificationQueuer notificationQueuer;
  private final @NonNull Collection<NotificationProcessor> notificationProcessors;

  @Scheduled(fixedDelay = 20000)
  private void queue() {

  }
}
