package org.humancellatlas.ingest.messaging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {
    @InjectMocks
    private MessageService messageService;

    @Mock
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void testPublish() {
        //given:
        Message message = new Message("exchange", "routingKey", "payload");

        //when:
        messageService.publish(message);

        //then:
        verify(rabbitMessagingTemplate).convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
    }

    @Test
    public void testPublishMessageConversionException() {
        //given:
        Message message = new Message("", "", "");

        //when:
        doThrow(MessageConversionException.class).when(rabbitMessagingTemplate).convertAndSend("","","");

        //then:
        assertThatThrownBy(() -> { messageService.publish(message); }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert payload");
    }

    @Test
    public void testPublishMessagingException() {
        //given:
        Message message = new Message("", "", "");

        //when:
        doThrow(MessagingException.class).when(rabbitMessagingTemplate).convertAndSend("","","");

        //then:
        assertThatThrownBy(() -> { messageService.publish(message); }).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("There was a problem sending message");
    }

    @Configuration
    static class TestConfiguration {}

}
