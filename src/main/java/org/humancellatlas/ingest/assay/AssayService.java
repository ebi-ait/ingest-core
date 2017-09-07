package org.humancellatlas.ingest.assay;

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
public class AssayService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull AssayRepository assayRepository;

    public Assay addAssayToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Assay assay) {
        assay.addToSubmissionEnvelope(submissionEnvelope);
        return getAssayRepository().save(assay);
    }
}
