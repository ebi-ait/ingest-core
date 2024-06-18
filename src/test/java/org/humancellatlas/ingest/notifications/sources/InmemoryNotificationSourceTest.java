package org.humancellatlas.ingest.notifications.sources;

import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.notifications.model.Checksum;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.sources.impl.inmemory.InmemoryNotificationSource;
import org.junit.jupiter.api.Test;

public class InmemoryNotificationSourceTest {

  private Notification generateTestNotification(String checksumValue) {
    return Notification.buildNew()
        .metadata(new HashMap<>())
        .content("testcontent")
        .checksum(new Checksum("testtype", checksumValue))
        .build();
  }

  @Test
  public void testSupply() {
    Notification testNotification1 = generateTestNotification("testvalue1");
    Notification testNotification2 = generateTestNotification("testvalue2");
    NotificationSource testInmemorySource = new InmemoryNotificationSource();

    Assertions.assertThatCode(
            () -> testInmemorySource.supply(List.of(testNotification1, testNotification2)))
        .doesNotThrowAnyException();
  }

  @Test
  public void testStream() {
    Notification testNotification1 = generateTestNotification("testvalue1");
    Notification testNotification2 = generateTestNotification("testvalue2");
    NotificationSource testInmemorySource = new InmemoryNotificationSource();

    testInmemorySource.supply(List.of(testNotification1, testNotification2));

    Assertions.assertThat(testInmemorySource.stream())
        .containsSequence(testNotification1, testNotification2);

    Assertions.assertThat(testInmemorySource.stream()).isEmpty();
  }
}
