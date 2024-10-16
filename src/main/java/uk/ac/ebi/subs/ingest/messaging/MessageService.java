package uk.ac.ebi.subs.ingest.messaging;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class MessageService {
  @Autowired @NonNull private RabbitMessagingTemplate messagingTemplate;

  public void publish(Message message) {
    try {
      messagingTemplate.convertAndSend(
          message.getExchange(), message.getRoutingKey(), message.getPayload());
    } catch (MessageConversionException e) {
      throw new IllegalArgumentException(
          String.format("Unable to convert payload '%s'", message.getPayload()));
    } catch (MessagingException e) {
      throw new RuntimeException(
          String.format(
              "There was a problem sending message '%s' to exchange '%s', with routing key '%s'.",
              message.getPayload(), message.getExchange(), message.getRoutingKey()));
    }
    return;
  }
}
