package uk.ac.ebi.subs.ingest.notifications.sources;

import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.subs.ingest.notifications.model.Checksum;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.sources.impl.inmemory.InmemoryNotificationSource;

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
