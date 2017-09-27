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

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    private final @NonNull Queue<QueuedMessage> validationMessageBatch = new PriorityQueue<>(Comparator.comparing(QueuedMessage::getQueuedDate));
    private final @NonNull Queue<QueuedMessage> accessionMessageBatch = new PriorityQueue<>(Comparator.comparing(QueuedMessage::getQueuedDate));
    private final @NonNull Queue<QueuedMessage> exportMessageBatch = new PriorityQueue<>(Comparator.comparing(QueuedMessage::getQueuedDate));

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
        sendFromQueue(this.validationMessageBatch, 40);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendAccessionMessages(){
        sendFromQueue(this.accessionMessageBatch, 20);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendExportMessages(){
        sendFromQueue(this.exportMessageBatch, 10);
    }

    private void sendFromQueue(Queue<QueuedMessage> messageQueue, int delayTimeSeconds){
        while(!messageQueue.isEmpty()){
            QueuedMessage nextMessage = messageQueue.peek();
            Date messageWaitTime = Date.from(Instant.now().minus(delayTimeSeconds, ChronoUnit.SECONDS));
            if (nextMessage.getQueuedDate().before(messageWaitTime)) {
                QueuedMessage message = messageQueue.remove();
                this.rabbitMessagingTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
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
