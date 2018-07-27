package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;

import java.util.Date;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MessageSenderTest {

    @Test
    public void testQueueNewAssayMessage() {
        //given:
        RabbitMessagingTemplate messageQueueTemplate = mock(RabbitMessagingTemplate.class);
        MessageSender sender = new MessageSender(messageQueueTemplate);

        //and:
        ExportMessage message = new ExportMessage("", "", "", "", "", "", 0, 0);

        //when:
        Date timestamp = new Date();
        String exchange = "queue.exchange";
        String routingKey = "queue.route";
        sender.queueNewExportMessage(exchange, routingKey, message);

        //then:
        verify(messageQueueTemplate).convertAndSend(exchange, routingKey, message);
    }

}
