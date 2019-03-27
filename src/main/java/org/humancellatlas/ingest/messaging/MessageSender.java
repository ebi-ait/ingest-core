package org.humancellatlas.ingest.messaging;

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

import java.net.URI;
import java.util.concurrent.*;

@Service
@Getter
@NoArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;
    private final @NonNull RestTemplate restTemplate = new RestTemplate();
    private @Autowired @NonNull ConfigurationService configurationService;


    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        this.rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueAccessionMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        this.rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload, long intendedSendTime){
        this.rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedSendTime){
        this.rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueDocumentStateUpdateMessage(URI uri, AbstractEntityMessage payload, long intendedSendTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");
        restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(payload, headers), Object.class);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload, long intendedSendTime) {
        this.rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
