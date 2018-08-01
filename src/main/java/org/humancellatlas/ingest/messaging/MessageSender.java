package org.humancellatlas.ingest.messaging;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.messaging.model.AbstractEntityMessage;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload){
        MessageBuffer.VALIDATION.queue(exchange, routingKey, payload);
    }

    public void queueAccessionMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload){
        MessageBuffer.ACCESSIONER.queue(exchange, routingKey, payload);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload){
        MessageBuffer.EXPORT.queue(exchange, routingKey, payload);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey,
            AbstractEntityMessage payload){
        MessageBuffer.STATE_TRACKING.queue(exchange, routingKey, payload);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload) {
        MessageBuffer.UPLOAD_MANAGER.queue(exchange, routingKey, payload);
    }


    @Scheduled(fixedDelay = 1000)
    private void sendValidationMessages() {
        MessageBuffer.VALIDATION.send(rabbitMessagingTemplate);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendAccessionMessages() {
        MessageBuffer.ACCESSIONER.send(rabbitMessagingTemplate);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendExportMessages(){
        MessageBuffer.EXPORT.send(rabbitMessagingTemplate);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendStateTrackerMessages() {
        MessageBuffer.STATE_TRACKING.send(rabbitMessagingTemplate);
    }

    @Scheduled(fixedDelay = 1000)
    private void sendUploadManagerMessages() {
        MessageBuffer.UPLOAD_MANAGER.send(rabbitMessagingTemplate);
    }

    @Data
    private static class QueuedMessage implements Delayed {

        private String exchange;
        private String routingKey;
        private AbstractEntityMessage payload;

        private Long delayMs = 0L;

        public QueuedMessage(String exchange, String routingKey, AbstractEntityMessage payload,
                Long delayMs) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
            this.delayMs = delayMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return MICROSECONDS.convert(delayMs, unit);
        }

        @Override
        public int compareTo(Delayed other) {
            long otherDelay = other.getDelay(MICROSECONDS);
            return (int) (this.delayMs - otherDelay);
        }

    }

    private enum MessageBuffer {

        VALIDATION(SECONDS.toMicros(3)),
        EXPORT(SECONDS.toMicros(5)),
        UPLOAD_MANAGER(SECONDS.toMicros(1)),
        ACCESSIONER(SECONDS.toMicros(2)),
        STATE_TRACKING(0L);

        private final Long delayMs;

        private final BlockingQueue<QueuedMessage> messageQueue = new DelayQueue<>();

        MessageBuffer(Long delayInMicroseconds) {
            this.delayMs = delayInMicroseconds;
        }

        //TODO each enum should already know exchange and routing key
        //Why was this part of the contract when they're already defined in Constants?
        void queue(String exchange, String routingKey, AbstractEntityMessage payload) {
            try {
                QueuedMessage message = new QueuedMessage(exchange, routingKey, payload, delayMs);
                messageQueue.put(message);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        void send(RabbitMessagingTemplate messagingTemplate) {
            try {
                QueuedMessage message = messageQueue.take();
                LOGGER.debug("Sending message to [%s]...", message.getExchange());
                messagingTemplate.convertAndSend(message.exchange, message.routingKey,
                        message.payload);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

    }

}
