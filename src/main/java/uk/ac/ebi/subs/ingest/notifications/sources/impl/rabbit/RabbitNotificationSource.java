package uk.ac.ebi.subs.ingest.notifications.sources.impl.rabbit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.messaging.Constants;
import uk.ac.ebi.subs.ingest.notifications.model.Notification;
import uk.ac.ebi.subs.ingest.notifications.sources.NotificationSource;
import uk.ac.ebi.subs.ingest.notifications.sources.impl.inmemory.InmemoryNotificationSource;

@Component
@RequiredArgsConstructor
public class RabbitNotificationSource implements NotificationSource {

  private final InmemoryNotificationSource inmemoryNotificationSource =
      new InmemoryNotificationSource();
  private final RabbitMessagingTemplate rabbitMessagingTemplate;
  private final AmqpConfig amqpConfig;

  private static String jsonString(Notification notification) {
    try {
      return new ObjectMapper()
          .registerModules(new JavaTimeModule())
          .writeValueAsString(notification);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Notification fromJsonString(String notification) {
    try {
      return new ObjectMapper()
          .registerModules(new JavaTimeModule())
          .readValue(notification, Notification.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @RabbitListener(queues = Constants.Queues.NOTIFICATIONS_QUEUE)
  private void listen(String notification) {
    this.inmemoryNotificationSource.supply(Collections.singletonList(fromJsonString(notification)));
  }

  @Override
  public Stream<Notification> stream() {
    return this.inmemoryNotificationSource.stream();
  }

  @Override
  public void supply(List<Notification> notifications) {
    notifications.forEach(
        notification -> {
          this.rabbitMessagingTemplate.convertAndSend(
              amqpConfig.getSendExchange(),
              amqpConfig.getSendRoutingKey(),
              jsonString(notification));
        });
  }
}
