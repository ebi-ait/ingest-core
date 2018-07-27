package org.humancellatlas.ingest.messaging;

import lombok.*;
import org.humancellatlas.ingest.messaging.model.AbstractEntityMessage;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSender {

    private @Autowired @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    public void queueValidationMessage(String exchange, String routingKey,
            MetadataDocumentMessage payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueAccessionMessage(String exchange, String routingKey, MetadataDocumentMessage
            payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueNewExportMessage(String exchange, String routingKey, ExportMessage payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueStateTrackingMessage(String exchange, String routingKey,
            AbstractEntityMessage payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
    }

    public void queueUploadManagerMessage(String exchange, String routingKey,
            SubmissionEnvelopeMessage payload) {
        rabbitMessagingTemplate.convertAndSend(exchange, routingKey, payload);
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
