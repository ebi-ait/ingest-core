package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.SubmissionStatus;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.messaging.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Service
public class SubmissionService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private AmqpTemplate messagingTemplate;

    public SubmissionReceipt submitEnvelope(SubmissionEnvelope submissionEnvelope) {
        log.info(String.format("Congratulations! You have submitted your envelope '%s'", submissionEnvelope.getId()));
        SubmissionEnvelope updatedEnvelope = submissionEnvelopeRepository.findOne(submissionEnvelope.getId());
        updatedEnvelope.setSubmissionStatus(SubmissionStatus.SUBMITTED);
        submissionEnvelopeRepository.save(updatedEnvelope);

        // post event to queue
        messagingTemplate.convertAndSend(Constants.Exchanges.ENVELOPE_FANOUT,"", new SubmissionEnvelopeMessage(submissionEnvelope));

        return new SubmissionReceipt();
    }
}
