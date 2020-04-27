package org.humancellatlas.ingest.notifications;

import java.util.Collection;
import java.util.Collections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NotificationCoordinator {

  private final @NonNull Collection<NotificationProcessor> notificationProcessors;
  private final @NonNull NotificationSource notificationSource;
  private final @NonNull NotificationService notificationService;

  @Scheduled(fixedDelay = 20000)
  private void queue() {
    this.notificationService.getUnhandledNotifications()
                            .forEach(notification -> {
                              this.notificationSource.supply(Collections.singletonList(notification));
                              this.notificationService.changeState(notification, NotificationState.QUEUED);
                            });
  }

  @Scheduled(fixedDelay = 10000)
  private void process() {
    this.notificationSource.stream()
                           .forEach(notification -> {
                             this.notificationService.changeState(notification, NotificationState.PROCESSING);
                             this.notificationProcessors.forEach(notificationProcessor -> {
                               notificationProcessor.handle(notification);
                             });
                             this.notificationService.changeState(notification, NotificationState.PROCESSED);
                           });
  }
}
