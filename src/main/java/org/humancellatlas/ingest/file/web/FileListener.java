package org.humancellatlas.ingest.file.web;

import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.messaging.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/** Created by rolando on 07/09/2017. */
@Component
@AllArgsConstructor
public class FileListener {
  private final @NonNull FileService fileService;
  private final Logger log = LoggerFactory.getLogger(getClass());

  @RabbitListener(queues = Constants.Queues.FILE_STAGED_QUEUE)
  public void handleFileStagedEvent(FileMessage fileMessage) {
    if (!StringUtils.isEmpty(fileMessage.getContentType())
        && fileMessage.getMediaType().isPresent()
        && fileMessage.getMediaType().get().equals(FileMediaTypes.HCA_DATA_FILE)) {
      try {
        fileService.createFileFromFileMessage(fileMessage);
        fileService.updateFileFromFileMessage(fileMessage);
      } catch (CoreEntityNotFoundException e) {
        log.warn(e.getMessage());
        throw new AmqpRejectAndDontRequeueException(e.getMessage());
      } catch (OptimisticLockingFailureException e) {
        log.warn("Putting file back on queue: " + e.getMessage());
        throw new ImmediateRequeueAmqpException(e);
      } catch (RuntimeException e) {
        log.error(e.getMessage());
        throw new AmqpRejectAndDontRequeueException(e.getMessage());
      }
    }
  }
}
