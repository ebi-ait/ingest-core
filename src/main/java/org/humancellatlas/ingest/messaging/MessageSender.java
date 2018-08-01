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

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.*;

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

        private final String exchange;
        private final String routingKey;
        private final AbstractEntityMessage payload;

        private final long intendedStartTime;

        public QueuedMessage(String exchange, String routingKey, AbstractEntityMessage payload,
                long delayMillis) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
            this.intendedStartTime = System.currentTimeMillis() + delayMillis;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = intendedStartTime - System.currentTimeMillis();
            return unit.convert(delay, MILLISECONDS);
        }

        private long getDelay() {
            return getDelay(MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            long otherDelay = other.getDelay(MILLISECONDS);
            return Math.toIntExact(getDelay() - otherDelay);
        }

    }

    private enum MessageBuffer {

        VALIDATION(SECONDS.toMillis(3)),
        EXPORT(SECONDS.toMillis(5)),
        UPLOAD_MANAGER(SECONDS.toMillis(1)),
        ACCESSIONER(SECONDS.toMillis(2)),
        STATE_TRACKING(0L);

        private final Long delayMillis;

        private final BlockingQueue<QueuedMessage> messageQueue = new DelayQueue<>();

        MessageBuffer(Long delayMillis) {
            this.delayMillis = delayMillis;
        }

        //TODO each enum should already know exchange and routing key
        //Why was this part of the contract when they're already defined in Constants?
        void queue(String exchange, String routingKey, AbstractEntityMessage payload) {
            try {
                QueuedMessage message = new QueuedMessage(exchange, routingKey, payload,
                        delayMillis);
                messageQueue.put(message);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        void send(RabbitMessagingTemplate messagingTemplate) {
            try {
                QueuedMessage message = messageQueue.take();
                LOGGER.debug(format("Sending message to [%s]...", message.getExchange()));
                messagingTemplate.convertAndSend(message.exchange, message.routingKey,
                        message.payload);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

    }

}
