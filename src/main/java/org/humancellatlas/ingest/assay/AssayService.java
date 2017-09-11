package org.humancellatlas.ingest.assay;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
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
    private final @NonNull ApplicationEventPublisher applicationEventPublisher;


    public Assay addAssayToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Assay assay) {
        assay.addToSubmissionEnvelope(submissionEnvelope);
        applicationEventPublisher.publishEvent(new BeforeSaveEvent(assay));
        return getAssayRepository().save(assay);
    }
}
