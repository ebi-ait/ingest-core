package org.humancellatlas.ingest.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        MessageBuffer.VALIDATION.queue(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueAccessionMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        MessageBuffer.ACCESSIONER.queue(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload, long intendedSendTime){
        MessageBuffer.EXPORT.queue(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey,
            AbstractEntityMessage payload, long intendedSendTime){
        MessageBuffer.STATE_TRACKING.queue(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload, long intendedSendTime) {
        MessageBuffer.UPLOAD_MANAGER.queue(exchange, routingKey, payload ,intendedSendTime);
    }

    @PostConstruct
    private void initiateSending() {
        Arrays.stream(MessageBuffer.values())
              .forEach(buffer -> scheduler.scheduleWithFixedDelay(new BufferSender(buffer, rabbitMessagingTemplate),
                                                                  0,
                                                                  buffer.getDelayMillis(),
                                                                  TimeUnit.MILLISECONDS));
    }

    @Data
    static class QueuedMessage implements Delayed {

        private final String exchange;
        private final String routingKey;
        private final AbstractEntityMessage payload;

        private final long intendedStartTime;

        public QueuedMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedStartTime) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
            this.intendedStartTime = intendedStartTime;
        }


        @Override
        public long getDelay(TimeUnit unit) {
            long delay = intendedStartTime - System.currentTimeMillis();
            return unit.convert(delay, MILLISECONDS);
        }


        @Override
        public int compareTo(Delayed other) {
            long otherDelay = other.getDelay(MILLISECONDS);
            return Math.toIntExact(getDelay(TimeUnit.MILLISECONDS) - otherDelay);
      }
    }

    private enum MessageBuffer {

        VALIDATION(SECONDS.toMillis(3)),
        EXPORT(SECONDS.toMillis(5)),
        UPLOAD_MANAGER(SECONDS.toMillis(1)),
        ACCESSIONER(SECONDS.toMillis(2)),
        STATE_TRACKING(SECONDS.toMillis(3));

        @Getter
        private final Long delayMillis;

        private final BlockingQueue<QueuedMessage> messageQueue = new DelayQueue<>();

        private final Logger log = LoggerFactory.getLogger(getClass());

        protected Logger getLog() {
            return log;
        }

        MessageBuffer(Long delayMillis) {
            this.delayMillis = delayMillis;
        }

        //TODO each enum should already know exchange and routing key
        //Why are these part of the contract when they're already defined in Constants?
        void queue(String exchange, String routingKey, AbstractEntityMessage payload, long intendedStartTime) {
            try {
                QueuedMessage message = new QueuedMessage(exchange, routingKey, payload, intendedStartTime);
                messageQueue.put(message);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        private QueuedMessage take() {
            try {
                return this.messageQueue.take();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        public Stream<QueuedMessage> takeAll() {
            return Stream.generate(this::take)
                         .limit(messageQueue.size());
        }

    }

    private static class BufferSender implements Runnable {

        private final MessageBuffer buffer;
        private final RabbitMessagingTemplate messagingTemplate;
        private final Logger log = LoggerFactory.getLogger(BufferSender.class);

        private BufferSender(MessageBuffer buffer, RabbitMessagingTemplate messagingTemplate) {
            this.buffer = buffer;
            this.messagingTemplate = messagingTemplate;
        }

        @Override
        public void run() {
            buffer.takeAll().forEach(message -> {
                messagingTemplate.convertAndSend(message.exchange, message.routingKey, message.payload);
                AbstractEntityMessage payload = message.payload;
                log.debug(String.format("Publishing message on exchange %s, routingKey = %s",
                                        message.exchange,
                                        message.routingKey));
                log.debug(String.format("Message: %s", convertToString(payload)));
            });
        }

        private String convertToString(Object object) {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = "";

            try {
                jsonString = mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                log.debug(String.format("An error in converting message object to string occurred: %s", e.getMessage()));
            }

            return jsonString;
        }
    }
}
