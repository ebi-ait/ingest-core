package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.Constants;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by rolando on 07/09/2017.
 */
@Service
@AllArgsConstructor
public class AccessionMetadataService {
    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;
//
//    public void accessionMetadata(MetadataDocument document) {
//        rabbitMessagingTemplate.convertAndSend(Constants.Exchanges.ACCESSION, "", document);
//    }
}
