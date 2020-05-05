package org.humancellatlas.ingest.notifications;

import java.util.HashMap;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.notifications.exception.DuplicateNotification;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationRequest;
import org.humancellatlas.ingest.notifications.model.NotificationState;
import org.junit.jupiter.api.Test;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class NotificationServicesTest {
  private NotificationRepository notificationRepository = mock(NotificationRepository.class);
  private NotificationService notificationService = new NotificationService(notificationRepository);

  @Test
  public void testCreateNotification() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    NotificationRequest notificationRequest = new NotificationRequest("testcontent",
                                                                      new HashMap<>(),
                                                                      testChecksum);

    Mockito.doReturn(Notification.buildNew()
                                 .metadata(new HashMap<>())
                                 .content("testcontent")
                                 .checksum(testChecksum)
                                 .build())
           .when(notificationRepository).save(any(Notification.class));

    Notification createdNotification = notificationService.createNotification(notificationRequest);

    assertThat(createdNotification.getChecksum()).isEqualTo(testChecksum);
    assertThat(createdNotification.getMetadata()).isEqualTo(new HashMap<>());
    assertThat(createdNotification.getState()).isEqualTo(NotificationState.PENDING);
    assertThat(createdNotification.getContent()).isEqualTo("testcontent");
  }

  @Test
  public void testCreateDuplicateNotification() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    NotificationRequest notificationRequest = new NotificationRequest("testcontent",
                                                                      new HashMap<>(),
                                                                      testChecksum);

    Notification testExistingNotification = Notification.buildNew()
                                                        .metadata(new HashMap<>())
                                                        .content("testcontent")
                                                        .checksum(testChecksum)
                                                        .build();


    Mockito.doThrow(new DuplicateKeyException(""))
           .when(notificationRepository).save(any(Notification.class));

    Mockito.doReturn(Optional.of(testExistingNotification))
           .when(notificationRepository).findByChecksum_Value("testvalue");


    Assertions.assertThatExceptionOfType(DuplicateNotification.class)
              .isThrownBy(() -> notificationService.createNotification(notificationRequest));
  }

  @Test
  public void testRetrieveByChecksum() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");

    Mockito.doReturn(Optional.of(Notification.buildNew()
                                             .metadata(new HashMap<>())
                                             .content("testcontent")
                                             .checksum(testChecksum)
                                             .build()))
           .when(notificationRepository).findByChecksum(testChecksum);

    Assertions.assertThat(notificationService.retrieveForChecksum(testChecksum).getChecksum())
              .isEqualTo(testChecksum);
  }

  @Test
  public void testChangeState() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    Notification testNotification = Notification.buildNew()
                                                .metadata(new HashMap<>())
                                                .content("testcontent")
                                                .checksum(testChecksum)
                                                .build();


    Mockito.doAnswer(returnsFirstArg())
           .when(notificationRepository).save(any(Notification.class));


    Notification changedState = notificationService.changeState(testNotification, NotificationState.QUEUED);
    assertThat(changedState.getState()).isEqualTo(NotificationState.QUEUED);
  }

  @Test
  public void testIllegalStateChange() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    Notification testNotification = Notification.buildNew()
                                                .metadata(new HashMap<>())
                                                .content("testcontent")
                                                .checksum(testChecksum)
                                                .build();

    Mockito.doAnswer(returnsFirstArg())
           .when(notificationRepository).save(any(Notification.class));

    Assertions.assertThatExceptionOfType(IllegalStateException.class)
              .isThrownBy(() -> notificationService.changeState(testNotification, NotificationState.PROCESSED));
  }
}