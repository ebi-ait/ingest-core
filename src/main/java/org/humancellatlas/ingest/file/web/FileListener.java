package org.humancellatlas.ingest.file.web;

import lombok.AllArgsConstructor;

import lombok.NonNull;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.messaging.Constants;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Created by rolando on 07/09/2017.
 */
@Component
@AllArgsConstructor
public class FileListener {

    private final @NonNull FileService fileService;

    @RabbitListener(queues = Constants.Queues.FILE_STAGED)
    public void handleFileStagedEvent(FileMessage fileMessage) {
        try {
            fileService.updateStagedFileUrl(fileMessage.getEnvelopeUuid(),
                    fileMessage.getFileName(),
                    fileMessage.getCloudUrl());
        } catch (CoreEntityNotFoundException e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }

    }
}
