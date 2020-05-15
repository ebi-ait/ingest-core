package org.humancellatlas.ingest.notifications;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.notifications.exception.DuplicateNotification;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationRequest;
import org.humancellatlas.ingest.notifications.model.NotificationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final Logger log = LoggerFactory.getLogger(getClass());

  public Notification createNotification(NotificationRequest notificationRequest) {
    try {
      Notification notification = Notification.buildNew()
                                              .content(notificationRequest.getContent())
                                              .metadata(notificationRequest.getMetadata())
                                              .checksum(notificationRequest.getChecksum())
                                              .build();

      return this.notificationRepository.save(notification);
    } catch (DuplicateKeyException e) {
      String checksumValue = notificationRequest.getChecksum().getValue();
      String id = this.notificationRepository.findByChecksum_Value(checksumValue)
                                             .orElseThrow(() -> {
                                               throw new RuntimeException(e);
                                             })
                                             .getId();

      throw new DuplicateNotification(String.format("Notification checksum value already exists in notification %s", id));
    }
  }

  public Notification retrieveForChecksum(Checksum checksum) {
    Optional<Notification> maybeNotification = this.notificationRepository.findByChecksum(checksum);
    return maybeNotification.orElseThrow(() -> {
      throw new ResourceNotFoundException(String.format("Couldn't find checksum %s", checksum.toString()));
    });
  }

  public Notification changeState(Notification notification, NotificationState toState) {
    if (notification.getState().isLegalTransition(toState)) {
      notification.setState(toState);
      return this.notificationRepository.save(notification);
    } else {
      throw new IllegalStateException(
          String.format("Cannot transition notification with ID %s from state %s to %s",
                        notification.getId(),
                        notification.getState(),
                        toState));
    }
  }

  public Stream<Notification> getUnhandledNotifications() {
    return notificationRepository.findByStateOrderByNotifyAtDesc(NotificationState.PENDING);
  }

  public Stream<Notification> getHandledNotifications() {
    return notificationRepository.findByStateOrderByNotifyAtDesc(NotificationState.PROCESSED);
  }

  public void deleteNotification(Notification notification) {
    notificationRepository.delete(notification);
  }
}
