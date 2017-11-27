package org.humancellatlas.ingest.sample;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataReference;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public SubmissionEnvelope resolveSampleReferencesForSubmission(SubmissionEnvelope submissionEnvelope, MetadataReference sampleReference) {
        List<Sample> samples = new ArrayList<>();

        for (String sampleUuid : sampleReference.getUuids()) {
            Uuid sampleUuidObj = new Uuid(sampleUuid);
            Sample sample = getSampleRepository().findByUuid(sampleUuidObj);

            if (sample != null) {
                sample.addToSubmissionEnvelope(submissionEnvelope);
                samples.add(sample);
                getLog().info(String.format("Adding sample to submission envelope '%s'", sample.getId()));
            }
            else {
                getLog().warn(String.format(
                        "No Sample present with UUID '%s' - in future this will cause a critical error",
                        sampleUuid));
            }
        }

        getSampleRepository().save(samples);

        return submissionEnvelope;
    }
}
