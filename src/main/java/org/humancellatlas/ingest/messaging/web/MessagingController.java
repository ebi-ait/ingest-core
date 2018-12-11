package org.humancellatlas.ingest.messaging.web;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.Message;
import org.humancellatlas.ingest.messaging.MessageService;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@Getter
public class MessagingController {
    @NonNull
    private final MessageService messageService;

    @RequestMapping(path = "/messaging/fileUploadInfo",
            method = RequestMethod.POST,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> publishFileUploadInfo(@RequestBody Object uploadInfo){
        Message uploadInfoMessage = new Message(Constants.Exchanges.FILE_STAGED_EXCHANGE, Constants.Queues.FILE_STAGED, uploadInfo);
        getMessageService().publish(uploadInfoMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(path = "/messaging/fileValidationResult",
            method = RequestMethod.POST,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> publishFileValidationResult(@RequestBody Object validationResult){
        Message uploadInfoMessage = new Message(Constants.Exchanges.VALIDATION, Constants.Queues.FILE_VALIDATION, validationResult);
        getMessageService().publish(uploadInfoMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}