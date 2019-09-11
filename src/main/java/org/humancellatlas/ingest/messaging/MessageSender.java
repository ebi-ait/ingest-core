package org.humancellatlas.ingest.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.messaging.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
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
    private @Autowired @NonNull ConfigurationService configurationService;


    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        MessageBuffer.VALIDATION.queueAmqpMessage(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueAccessionMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        MessageBuffer.ACCESSIONER.queueAmqpMessage(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload, long intendedSendTime){
        MessageBuffer.EXPORT.queueAmqpMessage(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueNewExportMessage(String exchange, String routingKey, BundleUpdateMessage payload, long intendedSendTime){
        MessageBuffer.EXPORT.queueAmqpMessage(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedSendTime){
        MessageBuffer.STATE_TRACKING.queueAmqpMessage(exchange, routingKey, payload, intendedSendTime);
    }

    public void queueDocumentStateUpdateMessage(URI uri, AbstractEntityMessage payload, long intendedSendTime) {
        MessageBuffer.STATE_TRACKING.queueHttpMessage(uri, payload, intendedSendTime);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload, long intendedSendTime) {
        MessageBuffer.UPLOAD_MANAGER.queueAmqpMessage(exchange, routingKey, payload ,intendedSendTime);
    }

    public void queueSubmissionNeedsProcessingMessage(String exchange, String routingKey, SubmissionEnvelopeMessage payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    @PostConstruct
    private void initiateSending(){
        List<MessageBuffer> amqpMessageBuffers = Arrays.asList(
                MessageBuffer.ACCESSIONER,
                MessageBuffer.EXPORT,
                MessageBuffer.UPLOAD_MANAGER,
                MessageBuffer.VALIDATION,
                MessageBuffer.STATE_TRACKING);

        amqpMessageBuffers
                .forEach(buffer -> scheduler.scheduleWithFixedDelay(new AmqpHttpMixinBufferSender(buffer, new RestTemplate(), rabbitMessagingTemplate),
                        0,
                        buffer.getDelayMillis(),
                        TimeUnit.MILLISECONDS));
    }

    @Data
    static class QueuedMessage implements Delayed {

        private final MessageProtocol messageProtocol;
        private String exchange;
        private String routingKey;
        private URI uri;
        private final AbstractEntityMessage payload;

        private final long intendedStartTime;

        public QueuedMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedStartTime) {
            this.messageProtocol = MessageProtocol.AMQP;
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
            this.intendedStartTime = intendedStartTime;
        }

        public QueuedMessage(URI uri, AbstractEntityMessage payload, long intendedStartTime) {
            this.messageProtocol = MessageProtocol.HTTP;
            this.uri = uri;
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
        STATE_TRACKING(500L);

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
        void queueAmqpMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedStartTime) {
            QueuedMessage message = new QueuedMessage(exchange, routingKey, payload, intendedStartTime + delayMillis);
            try {
                messageQueue.add(message);
            } catch (IllegalStateException e) {
                LOGGER.error(String.format("Failed to queue message: %s", convertToString(message)), e);
                throw new RuntimeException(e);
            }
        }

        void queueHttpMessage(URI uri, AbstractEntityMessage payload, long intendedStartTime) {
            QueuedMessage message = new QueuedMessage(uri, payload, intendedStartTime + delayMillis);
            try {
                messageQueue.add(message);
            } catch (IllegalStateException e) {
                LOGGER.error(String.format("Failed to queue message: %s", convertToString(message)), e);
                throw new RuntimeException(e);
            }
        }

        public Stream<QueuedMessage> takeAll() {
            Queue<QueuedMessage> drainedQueue = new PriorityQueue<>(Comparator.comparing(QueuedMessage::getIntendedStartTime));
            this.messageQueue.drainTo(drainedQueue);
            return Stream.generate(drainedQueue::remove)
                         .limit(drainedQueue.size());
        }

        private String convertToString(Object object) {
            try {
                return new ObjectMapper().writeValueAsString(object);
            } catch (JsonProcessingException e) {
                LOGGER.debug(String.format("An error in converting message object to string occurred: %s", e.getMessage()));
                return "";
            }
        }
    }

    private static class AmqpHttpMixinBufferSender implements Runnable {
        private final MessageBuffer buffer;
        private final RestTemplate restTemplate;
        private final RabbitMessagingTemplate messagingTemplate;
        private final Logger log = LoggerFactory.getLogger(MessageSender.AmqpHttpMixinBufferSender.class);

        private AmqpHttpMixinBufferSender(MessageBuffer buffer, RestTemplate restTemplate, RabbitMessagingTemplate rabbitMessagingTemplate) {
            this.buffer = buffer;
            this.restTemplate = restTemplate;
            this.messagingTemplate = rabbitMessagingTemplate;
        }

        @Override
        public void run() {
            HttpHeaders headers = uriListHeaders();
            buffer.takeAll().forEach(message -> {
                if(message.getMessageProtocol().equals(MessageProtocol.AMQP)) {
                    messagingTemplate.convertAndSend(message.exchange, message.routingKey, message.payload);
                } else {
                    try {
                        restTemplate.exchange(message.getUri(), HttpMethod.POST, new HttpEntity<>(message.getPayload(), headers), Object.class);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        }

        private HttpHeaders uriListHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/json");
            return headers;
        }
    }
}
