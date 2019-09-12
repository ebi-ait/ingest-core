package org.humancellatlas.ingest.messaging;

import lombok.*;
import org.humancellatlas.ingest.messaging.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;
    private final @NonNull RestTemplate restTemplate = new RestTemplate();

    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload, long intendedSendTime){
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload, long intendedSendTime){
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueNewExportMessage(String exchange, String routingKey, BundleUpdateMessage payload, long intendedSendTime){
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey, AbstractEntityMessage payload, long intendedSendTime){
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueDocumentStateUpdateMessage(URI uri, AbstractEntityMessage payload) {
        restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(payload, applicationJsonHeaders()), Object.class);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload, long intendedSendTime) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    private static HttpHeaders applicationJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");
        return headers;
    }
}
