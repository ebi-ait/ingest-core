package org.humancellatlas.ingest.sample;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
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

    public Sample addSampleToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Sample sample) {
        Sample result = getSampleRepository().save(sample);
        getSubmissionEnvelopeRepository().save(submissionEnvelope.addSample(result));
        return result;
    }
}
