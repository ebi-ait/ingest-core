package org.humancellatlas.ingest.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessagingUtils {
    private final static Logger log = LoggerFactory.getLogger(MessagingUtils.class);

    public static void basicAck(Channel channel, long tag, boolean reject, boolean requeue) {
        try {
            if(reject) {
                channel.basicNack(tag, false, requeue);
            } else {
                channel.basicAck(tag, false);
            }
        } catch (IOException e) {
            log.error(String.format("Failed to ack() or nack() AMQP message with tag %s for queue %s",
                                    tag,
                                    Constants.Queues.SUBMISSION_PROCESSING));
        }
    }
}
