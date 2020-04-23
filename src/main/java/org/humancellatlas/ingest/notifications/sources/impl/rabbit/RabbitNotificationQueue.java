package org.humancellatlas.ingest.notifications.sources.impl.rabbit;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants.Queues;
import org.humancellatlas.ingest.notifications.Notification;
import org.humancellatlas.ingest.notifications.sources.NotificationSource;
import org.humancellatlas.ingest.notifications.sources.impl.inmemory.InmemoryNotificationQueue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;

@RequiredArgsConstructor
public class RabbitNotificationQueue implements NotificationSource {
  private final InmemoryNotificationQueue inmemoryNotificationQueue = new InmemoryNotificationQueue();
  private final RabbitMessagingTemplate rabbitMessagingTemplate;
  private final AmqpConfig amqpConfig;

  @RabbitListener(queues = Queues.NOTIFICATIONS_QUEUE)
  private void listen(Notification notification) {
    this.supply(Collections.singletonList(notification));
  }

  @Override
  public Stream<Notification> stream() {
    return this.inmemoryNotificationQueue.stream();
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
