package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.junit.Test;

import java.util.Date;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSenderTest {

    @Test
    public void testQueueNewAssayMessage() {
        //given:
        MessageSender sender = new MessageSender();

        //and:
        ExportMessage message = new ExportMessage("", "", "", "", "", "", 0, 0);

        //when:
        Date timestamp = new Date();
        sender.queueNewAssayMessage("queue.exchange", "queue.route", message);

        //then:
        Queue<MessageSender.QueuedMessage> queue = sender.getAssayMessageBatch();
        assertThat(queue).hasSize(1);

        //and:
        MessageSender.QueuedMessage queuedMessage = queue.peek();
        assertThat(queuedMessage)
                .extracting("exchange", "routingKey", "payload")
                .containsExactly("queue.exchange", "queue.route", message);

        //and:
        int _500MilliSeconds = 500;
        assertThat(queuedMessage.getQueuedDate()).isCloseTo(timestamp, _500MilliSeconds);
    }

}
