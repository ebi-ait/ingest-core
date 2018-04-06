package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.messaging.model.AssaySubmittedMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSenderTest {

    @Test
    public void testQueueNewAssayMessage() {
        //given:
        MessageSender sender = new MessageSender();

        //and:
        AssaySubmittedMessage message = new AssaySubmittedMessage("", "", "", "", "", "", 0, 0);

        //when:
        sender.queueNewAssayMessage("queue.exchange", "queue.route", message);

        //then:
        assertThat(sender.getAssayMessageBatch()).hasSize(1);
    }

}
