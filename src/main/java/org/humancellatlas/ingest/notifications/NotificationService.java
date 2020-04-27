package org.humancellatlas.ingest.notifications;

import java.time.Instant;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final Logger log = LoggerFactory.getLogger(getClass());

  public Notification createNotification(NotificationRequest notificationRequest) {
    Notification notification = Notification.builder()
                                            .content(notificationRequest.getContent())
                                            .metadata(notificationRequest.getMetadata())
                                            .state(NotificationState.REGISTERED)
                                            .checksum(notificationRequest.getChecksum())
                                            .notifyAt(Instant.now())
                                            .build();

    return this.notificationRepository.save(notification);
  }

  public Notification changeState(Notification notification, NotificationState toState) {
    if (notification.getState().legalTransitions().contains(toState)) {
      Notification changed = Notification.builder()
                                         .id(notification.getId())
                                         .content(notification.getContent())
                                         .metadata(notification.getMetadata())
                                         .state(toState)
                                         .notifyAt(notification.getNotifyAt())
                                         .build();

      return this.notificationRepository.save(changed);
    } else {
      throw new IllegalStateException(
          String.format("Cannot transition notification with ID %s from state %s to %s",
                        notification.getId(),
                        notification.getState(),
                        toState));
    }
  }

  public Stream<Notification> getUnhandledNotifications() {
    return notificationRepository.findByStateOrderByNotifyAtDesc(NotificationState.REGISTERED);
  }

}
