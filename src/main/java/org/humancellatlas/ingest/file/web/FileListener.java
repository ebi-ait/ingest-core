package org.humancellatlas.ingest.file.web;

import lombok.AllArgsConstructor;

import lombok.NonNull;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.messaging.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by rolando on 07/09/2017.
 */
@Component
@AllArgsConstructor
public class FileListener {
    private final @NonNull FileService fileService;
    private final Logger log = LoggerFactory.getLogger(getClass());


    @RabbitListener(queues = Constants.Queues.FILE_STAGED)
    public void handleFileStagedEvent(FileMessage fileMessage) {
        if(!StringUtils.isEmpty(fileMessage.getContentType())
                && fileMessage.getMediaType().isPresent()
                && fileMessage.getMediaType().get().equals(FileMediaTypes.HCA_DATA_FILE)){
            try {
                fileService.updateStagedFileUrl(fileMessage.getStagingAreaId(),
                                                fileMessage.getFileName(),
                                                fileMessage.getCloudUrl());
            } catch (CoreEntityNotFoundException | RuntimeException e) {
                throw new AmqpRejectAndDontRequeueException(e.getMessage());
            }
        }
    }
}
