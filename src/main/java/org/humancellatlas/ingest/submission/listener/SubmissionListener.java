package org.humancellatlas.ingest.submission.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SubmissionListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RabbitListener(queues = Constants.Queues.SUBMISSION_PROCESSING)
    public void processSubmissions(SubmissionEnvelopeMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {

    }
}
