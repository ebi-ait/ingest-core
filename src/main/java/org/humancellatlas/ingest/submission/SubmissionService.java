package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public SubmissionReceipt submitEnvelope(SubmissionEnvelope submissionEnvelope) {
        log.info(String.format("Congratulations! You have submitted your envelope '%s'", submissionEnvelope.getId()));
        return new SubmissionReceipt();
    }
}
