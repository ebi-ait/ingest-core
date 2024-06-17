package org.humancellatlas.ingest.messaging.web;

import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.Message;
import org.humancellatlas.ingest.messaging.MessageService;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Getter
public class MessagingController {
  @NonNull private final MessageService messageService;

  @PostMapping(
      path = "/messaging/fileUploadInfo",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaTypes.HAL_JSON_VALUE)
  ResponseEntity<Resource<?>> publishFileUploadInfo(@RequestBody ObjectNode uploadInfo) {
    Message uploadInfoMessage =
        new Message(
            Constants.Exchanges.FILE_STAGED_EXCHANGE,
            Constants.Queues.FILE_STAGED_QUEUE,
            uploadInfo);
    getMessageService().publish(uploadInfoMessage);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(
      path = "/messaging/fileValidationResult",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaTypes.HAL_JSON_VALUE)
  ResponseEntity<Resource<?>> publishFileValidationResult(
      @RequestBody ObjectNode validationResult) {
    Message uploadInfoMessage =
        new Message(
            Constants.Exchanges.VALIDATION_EXCHANGE,
            Constants.Queues.FILE_VALIDATION_QUEUE,
            validationResult);
    getMessageService().publish(uploadInfoMessage);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
