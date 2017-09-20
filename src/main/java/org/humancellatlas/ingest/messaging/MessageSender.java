package org.humancellatlas.ingest.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Getter
public class MessageSender {

    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;
    private final @NonNull Queue<QueuedMessage> messageQueue;

    @Autowired public MessageSender(RabbitMessagingTemplate rabbitMessagingTemplate){
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.messageQueue = new PriorityQueue<>(Comparator.comparing(QueuedMessage::getQueuedDate));
    }

    public void queueMessage(String exchange, String routingKey, Object payload){
        QueuedMessage message = new QueuedMessage(new Date(), exchange, routingKey, payload);
        this.messageQueue.add(message);
    }

    @Scheduled(fixedDelay = 1000)
    private void send(){
        while(!messageQueue.isEmpty()){
            QueuedMessage nextMessage = messageQueue.peek();
            // get one minute ago
            Date oneMinuteAgo = Date.from(Instant.now().minus(60, ChronoUnit.SECONDS));
            if (nextMessage.getQueuedDate().before(oneMinuteAgo)) {
                QueuedMessage message = messageQueue.remove();
                this.rabbitMessagingTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
            }
            else {
                break;
            }
        }
    }

    @Data
    @AllArgsConstructor
    class QueuedMessage {
        private Date queuedDate;
        private String exchange;
        private String routingKey;
        private Object payload;
    }
}
