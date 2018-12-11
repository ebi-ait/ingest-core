package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.file.web.FileController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={ MessageService.class })
public class MessageServiceTest {
    @Autowired
    private MessageService messageService;

    @MockBean
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
