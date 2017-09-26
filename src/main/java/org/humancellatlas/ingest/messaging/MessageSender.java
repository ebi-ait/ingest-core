package org.humancellatlas.ingest.messaging;

import lombok.*;

import org.humancellatlas.ingest.core.MetadataDocumentMessage;
import org.humancellatlas.ingest.core.AbstractEntityMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessage;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    private final @NonNull Set<QueuedMessage> validationMessageBatch = Collections.newSetFromMap(new ConcurrentHashMap<QueuedMessage, Boolean>());
    private final @NonNull Set<QueuedMessage> accessionMessageBatch = Collections.newSetFromMap(new ConcurrentHashMap<QueuedMessage, Boolean>());
    private final @NonNull Set<QueuedMessage> exportMessageBatch = Collections.newSetFromMap(new ConcurrentHashMap<QueuedMessage, Boolean>());

    private Map<String, Boolean> processedIds = new HashMap<>();


    public void queueValidationMessage(String exchange, String routingKey, MetadataDocumentMessage payload){
        QueuedMessage message = new QueuedMessage(new Date(), exchange, routingKey, payload);
        this.validationMessageBatch.add(message);
    }

    public void queueAccessionMessage(String exchange, String routingKey, MetadataDocumentMessage payload){
        QueuedMessage message = new QueuedMessage(new Date(), exchange, routingKey, payload);
        this.accessionMessageBatch.add(message);
    }

    public void queueExportMessage(String exchange, String routingKey, SubmissionEnvelopeMessage payload){
        QueuedMessage message = new QueuedMessage(new Date(), exchange, routingKey, payload);
        this.exportMessageBatch.add(message);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendValidationMessages(){
        sendFromQueue(this.validationMessageBatch, 60);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendAccessionMessages(){
        sendFromQueue(this.accessionMessageBatch, 30);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendExportMessages(){
        sendFromQueue(this.exportMessageBatch, 15);
    }

    private void sendFromQueue(Set<QueuedMessage> messageBatch, int delayTimeSeconds){
        ArrayList<QueuedMessage> messages = new ArrayList<>(messageBatch);
        messages.sort(Comparator.comparing(QueuedMessage::getQueuedDate));
        for (QueuedMessage message : messages) {
            Date messageWaitTime = Date.from(Instant.now().minus(delayTimeSeconds, ChronoUnit.SECONDS));
            if (message.getQueuedDate().before(messageWaitTime)) {
                this.rabbitMessagingTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
                messageBatch.remove(message);
            } else {
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
        private AbstractEntityMessage payload;

        @Override
        public boolean equals(Object qm) {
            QueuedMessage that = (QueuedMessage) qm;
            return  this.hashCode() == that.hashCode() &&
                    this.getRoutingKey().equals(that.getRoutingKey()) &&
                    this.getPayload().getDocumentId().equals(that.getPayload().getDocumentId());
        }

        @Override
        public int hashCode(){
            return Objects.hash(this.getRoutingKey(), this.getPayload().getDocumentId());
        }
    }
}
