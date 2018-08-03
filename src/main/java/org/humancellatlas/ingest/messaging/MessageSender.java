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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private static final long TASK_DELAY_MILLIS = 500;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

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

    @PostConstruct
    private void initiateSending() {
        Arrays.stream(MessageBuffer.values())
                .map(buffer -> new BufferSender(buffer, rabbitMessagingTemplate))
                .forEach(threadPool::submit);
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
        //Why are these part of the contract when they're already defined in Constants?
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
                messagingTemplate.convertAndSend(message.exchange, message.routingKey,
                        message.payload);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

    }

    private static class BufferSender implements Runnable {

        private final MessageBuffer buffer;
        private final RabbitMessagingTemplate messagingTemplate;

        private BufferSender(MessageBuffer buffer, RabbitMessagingTemplate messagingTemplate) {
            this.buffer = buffer;
            this.messagingTemplate = messagingTemplate;
        }

        @Override
        public void run() {
            while (true) {
                buffer.send(messagingTemplate);
            }
        }

    }

}
