package org.humancellatlas.ingest.messaging.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Message;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Getter
public class MessagingController {
    @Autowired
    @NonNull
    public RabbitMessagingTemplate messagingTemplate;

    @RequestMapping(path = "/messaging/publish",
            method = RequestMethod.POST,
            produces = MediaTypes.HAL_JSON_VALUE)
    ResponseEntity<Resource<?>> publish(@RequestBody Message message){
        try {
            messagingTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
        } catch(MessageConversionException e){
            throw new IllegalArgumentException(String.format("Unable to convert payload '%s'", message.getPayload()));
        } catch(MessagingException e){
            throw new RuntimeException(String.format(
                "There was a problem sending message '%s' to exchange '%s', with routing key '%s'.",
                message.getPayload(), message.getExchange(), message.getRoutingKey()));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
