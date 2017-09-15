package org.humancellatlas.ingest.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
@Getter
public class MessageSender {

    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;
    private final @NonNull Queue<QueuedMessage> messageQueue;

    @Autowired public MessageSender(RabbitMessagingTemplate rabbitMessagingTemplate){
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.messageQueue = new LinkedList<>();
    }

    public void queueMessage(String exchange, String routingKey, Object payload){
        QueuedMessage message = new QueuedMessage(exchange, routingKey, payload);
        this.messageQueue.add(message);
    }

    @Scheduled(fixedDelay = 60000)
    private void send(){
        if(!messageQueue.isEmpty()){
            QueuedMessage message = messageQueue.remove();
            this.rabbitMessagingTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
        }
    }

    @Data
    @AllArgsConstructor
    class QueuedMessage {
        private String exchange;
        private String routingKey;
        private Object payload;
    }
}
