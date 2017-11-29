package org.humancellatlas.ingest.sample;

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
public class SampleService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull SampleRepository sampleRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Sample addSampleToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Sample sample) {
        sample.addToSubmissionEnvelope(submissionEnvelope);
        return getSampleRepository().save(sample);
    }

}
