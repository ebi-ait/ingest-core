package org.humancellatlas.ingest.notifications;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.notifications.exception.ProcessingException;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.model.NotificationState;
import org.humancellatlas.ingest.notifications.processors.NotificationProcessor;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.humancellatlas.ingest.notifications.sources.impl.inmemory.InmemoryNotificationSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class NotificationCoordinatorTest {

  private Notification generateTestNotification(String checksumValue) {
    return Notification.buildNew()
        .metadata(new HashMap<>())
        .content("testcontent")
        .checksum(new Checksum("testtype", checksumValue))
        .build();
  }

  private NotificationService mockNotificationService() {
    NotificationService notificationService = mock(NotificationService.class);

    Answer<Notification> mockChangeStateFn =
        invocation -> {
          Notification notification = invocation.getArgument(0);
          NotificationState toState = invocation.getArgument(1);
          return Notification.builder()
              .checksum(notification.getChecksum())
              .content(notification.getContent())
              .metadata(notification.getMetadata())
              .notifyAt(notification.getNotifyAt())
              .state(toState)
              .build();
        };

    Mockito.doAnswer(mockChangeStateFn)
        .when(notificationService)
        .changeState(any(Notification.class), any(NotificationState.class));

    return notificationService;
  }

  @Test
  public void testQueue() {
    List<Notification> testNotifications =
        List.of(generateTestNotification("testvalue1"), generateTestNotification("testvalue2"));
    NotificationSource testInmemorySource = new InmemoryNotificationSource();

    NotificationService notificationService = mockNotificationService();
    Mockito.doReturn(testNotifications.stream())
        .when(notificationService)
        .getUnhandledNotifications();

    NotificationCoordinator notificationCoordinator =
        new NotificationCoordinator(
            Collections.emptyList(), testInmemorySource, notificationService);

    notificationCoordinator.queue();

    Assertions.assertThat(testInmemorySource.stream().map(n -> n.getChecksum().getValue()))
        .containsSequence(
            testNotifications.stream()
                .map(n -> n.getChecksum().getValue())
                .collect(Collectors.toList()));
  }

  @Test
  public void testProcess() {
    NotificationSource mockInmemorySource = mock(NotificationSource.class);
    Mockito.doReturn(
        Stream.of(generateTestNotification("testvalue1"), generateTestNotification("testvalue2")))
        .when(mockInmemorySource)
        .stream();

    NotificationService notificationService = mockNotificationService();

    NotificationProcessor mockHappyNotificationProcessor = mock(NotificationProcessor.class);
    Mockito.doNothing().when(mockHappyNotificationProcessor).handle(any(Notification.class));

    NotificationCoordinator notificationCoordinator =
        new NotificationCoordinator(
            List.of(mockHappyNotificationProcessor), mockInmemorySource, notificationService);

    notificationCoordinator.process();

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.PROCESSING));

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.PROCESSED));
  }

  @Test
  public void testProcessingFailure() {
    NotificationSource mockInmemorySource = mock(NotificationSource.class);
    Mockito.doReturn(
        Stream.of(generateTestNotification("testvalue1"), generateTestNotification("testvalue2")))
        .when(mockInmemorySource)
        .stream();

    NotificationService notificationService = mockNotificationService();

    NotificationProcessor mockUnhappyNotificationProcessor = mock(NotificationProcessor.class);
    Mockito.doThrow(new ProcessingException(""))
        .when(mockUnhappyNotificationProcessor)
        .handle(any(Notification.class));
    Mockito.doReturn(true)
        .when(mockUnhappyNotificationProcessor)
        .isEligible(any(Notification.class));

    NotificationCoordinator notificationCoordinator =
        new NotificationCoordinator(
            List.of(mockUnhappyNotificationProcessor), mockInmemorySource, notificationService);

    notificationCoordinator.process();

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.PROCESSING));

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.FAILED));
  }

  /**
   * Test behaviour when one processor succeeds, but another fails. Expect an overall failure
   * outcome.
   */
  @Test
  public void testMultipleProcessors_OneFailing() {
    NotificationSource fakeSource = new InmemoryNotificationSource();
    NotificationService notificationService = mockNotificationService();

    NotificationProcessor mockHappyNotificationProcessor = mock(NotificationProcessor.class);
    Mockito.doNothing().when(mockHappyNotificationProcessor).handle(any(Notification.class));
    Mockito.doReturn(true).when(mockHappyNotificationProcessor).isEligible(any(Notification.class));

    NotificationProcessor mockUnhappyNotificationProcessor = mock(NotificationProcessor.class);
    Mockito.doThrow(new ProcessingException(""))
        .when(mockUnhappyNotificationProcessor)
        .handle(any(Notification.class));
    Mockito.doReturn(true)
        .when(mockUnhappyNotificationProcessor)
        .isEligible(any(Notification.class));

    NotificationCoordinator notificationCoordinator =
        new NotificationCoordinator(
            List.of(mockUnhappyNotificationProcessor, mockHappyNotificationProcessor),
            fakeSource,
            notificationService);

    fakeSource.supply(
        List.of(generateTestNotification("testvalue1"), generateTestNotification("testvalue2")));
    notificationCoordinator.process();

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.PROCESSING));

    Mockito.verify(notificationService, times(2))
        .changeState(any(Notification.class), eq(NotificationState.FAILED));

    Mockito.verify(notificationService, never())
        .changeState(any(Notification.class), eq(NotificationState.PROCESSED));
  }
}
