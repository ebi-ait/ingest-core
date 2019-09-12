package org.humancellatlas.ingest.submission.listener;

import com.rabbitmq.client.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class SubmissionListener {
    private final @NonNull SubmissionHandler submissionHandler;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RabbitListener(queues = Constants.Queues.SUBMISSION_PROCESSING)
    public void processSubmissions(SubmissionEnvelopeMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        String submissionId = message.getDocumentId();
        submissionHandler.handleProcessSubmission(submissionId)
                         .thenRun(() -> {
                             log.info(String.format("Completed submission-processing for submission with id %s", submissionId));
                             basicAck(channel, tag, false, false);
                         })
                         .exceptionally(ex -> {
                             log.warn(String.format("Failed submission-processing for submission with id %s", submissionId), ex);
                             basicAck(channel, tag, true, false);
                             return null; // return Void
                         });
    }

    private void basicAck(Channel channel, long tag, boolean reject, boolean requeue) {
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
