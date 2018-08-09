package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.messaging.model.AbstractEntityMessage;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class MessageSenderTest {

    @Autowired
    private MessageSender sender;

    @MockBean
    private RabbitMessagingTemplate messagingTemplate;

    @Test
    public void testQueueNewAssayMessage() {
        //given:
        ExportMessage message = new ExportMessage("", "", "", "", "", "", 0, 0);

        //when:
        Date timestamp = new Date();
        sender.queueNewExportMessage("queue.exchange", "queue.route", message, System.currentTimeMillis());

        //then:
        verify(messagingTemplate, timeout(SECONDS.toMillis(10)))
                .convertAndSend(eq("queue.exchange"), eq("queue.route"),
                        any(AbstractEntityMessage.class));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        MessageSender messageSender() {
            return new MessageSender();
        }

    }

}
