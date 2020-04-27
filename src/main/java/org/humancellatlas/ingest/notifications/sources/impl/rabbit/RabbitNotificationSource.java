package org.humancellatlas.ingest.notifications.sources.impl.rabbit;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants.Queues;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.humancellatlas.ingest.notifications.sources.impl.inmemory.InmemoryNotificationSource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(value = "rabbitNotificationSource")
public class RabbitNotificationSource implements NotificationSource {
  private final InmemoryNotificationSource inmemoryNotificationSource = new InmemoryNotificationSource();
  private final RabbitMessagingTemplate rabbitMessagingTemplate;
  private final AmqpConfig amqpConfig;

  @RabbitListener(queues = Queues.NOTIFICATIONS_QUEUE)
  private void listen(Notification notification) {
    this.supply(Collections.singletonList(notification));
  }

  @Override
  public Stream<Notification> stream() {
    return this.inmemoryNotificationSource.stream();
  }

  @Override
  public void supply(List<Notification> notifications) {
    notifications.forEach(notification -> {
      this.rabbitMessagingTemplate.convertAndSend(amqpConfig.getSendExchange(),
                                                  amqpConfig.getSendRoutingKey(),
                                                  notification);
    });
  }
}
