package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.ValidationMessage;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by rolando on 06/09/2017.
 */
@Service
@AllArgsConstructor
public class ValidateMetadataService {
    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    public void validateMetadata(MetadataDocument document){
        ValidationMessage validationMessage = new ValidationMessage(document.getType(), document.getUuid(), document.getContent());
        rabbitMessagingTemplate.convertAndSend(Constants.Exchanges.VALIDATION, "", validationMessage);
    }
}