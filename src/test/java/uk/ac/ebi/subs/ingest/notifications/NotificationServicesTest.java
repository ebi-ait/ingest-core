package uk.ac.ebi.subs.ingest.notifications;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;

import uk.ac.ebi.subs.ingest.notifications.exception.DuplicateNotification;
import uk.ac.ebi.subs.ingest.notifications.model.Checksum;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.model.NotificationRequest;
import uk.ac.ebi.subs.ingest.notifications.model.NotificationState;

public class NotificationServicesTest {
  private NotificationRepository notificationRepository = mock(NotificationRepository.class);
  private NotificationService notificationService = new NotificationService(notificationRepository);

  @Test
  public void testCreateNotification() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    NotificationRequest notificationRequest =
        new NotificationRequest("testcontent", new HashMap<>(), testChecksum);

    Mockito.doReturn(
            Notification.buildNew()
                .metadata(new HashMap<>())
                .content("testcontent")
                .checksum(testChecksum)
                .build())
        .when(notificationRepository)
        .save(any(Notification.class));

    Notification createdNotification = notificationService.createNotification(notificationRequest);

    assertThat(createdNotification.getChecksum()).isEqualTo(testChecksum);
    assertThat(createdNotification.getMetadata()).isEqualTo(new HashMap<>());
    assertThat(createdNotification.getState()).isEqualTo(NotificationState.PENDING);
    assertThat(createdNotification.getContent()).isEqualTo("testcontent");
  }

  @Test
  public void testCreateDuplicateNotification() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    NotificationRequest notificationRequest =
        new NotificationRequest("testcontent", new HashMap<>(), testChecksum);

    Notification testExistingNotification =
        Notification.buildNew()
            .metadata(new HashMap<>())
            .content("testcontent")
            .checksum(testChecksum)
            .build();

    Mockito.doThrow(new DuplicateKeyException(""))
        .when(notificationRepository)
        .save(any(Notification.class));

    Mockito.doReturn(Optional.of(testExistingNotification))
        .when(notificationRepository)
        .findByChecksum_Value("testvalue");

    Assertions.assertThatExceptionOfType(DuplicateNotification.class)
        .isThrownBy(() -> notificationService.createNotification(notificationRequest));
  }

  @Test
  public void testRetrieveByChecksum() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");

    Mockito.doReturn(
            Optional.of(
                Notification.buildNew()
                    .metadata(new HashMap<>())
                    .content("testcontent")
                    .checksum(testChecksum)
                    .build()))
        .when(notificationRepository)
        .findByChecksum(testChecksum);

    Assertions.assertThat(
            notificationService.retrieveForChecksum(testChecksum).orElseThrow().getChecksum())
        .isEqualTo(testChecksum);
  }

  @Test
  public void testChangeState() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    Notification testNotification =
        Notification.buildNew()
            .metadata(new HashMap<>())
            .content("testcontent")
            .checksum(testChecksum)
            .build();

    Mockito.doAnswer(returnsFirstArg()).when(notificationRepository).save(any(Notification.class));

    Notification changedState =
        notificationService.changeState(testNotification, NotificationState.QUEUED);
    assertThat(changedState.getState()).isEqualTo(NotificationState.QUEUED);
  }

  @Test
  public void testIllegalStateChange() {
    Checksum testChecksum = new Checksum("testtype", "testvalue");
    Notification testNotification =
        Notification.buildNew()
            .metadata(new HashMap<>())
            .content("testcontent")
            .checksum(testChecksum)
            .build();

    Mockito.doAnswer(returnsFirstArg()).when(notificationRepository).save(any(Notification.class));

    Assertions.assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () -> notificationService.changeState(testNotification, NotificationState.PROCESSED));
  }
}
