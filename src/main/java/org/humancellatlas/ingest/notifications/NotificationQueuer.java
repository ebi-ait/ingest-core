package org.humancellatlas.ingest.notifications;

import java.util.Collections;
import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class NotificationQueuer {

  private final NotificationService notificationService;
  private final NotificationSource notificationSource;

  public void queue() {
    this.notificationService.getUnhandledNotifications()
                            .forEach(notification -> {
                              this.notificationSource.supply(Collections.singletonList(notification));
                              this.notificationService.changeState(notification, NotificationState.QUEUED);
                            });
  }


}
