package org.humancellatlas.ingest.assay;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class AssayService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull AssayRepository assayRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }
    public Assay addAssayToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Assay assay) {
        assay.addToSubmissionEnvelope(submissionEnvelope);
        return getAssayRepository().save(assay);
    }

}
