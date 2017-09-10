package org.humancellatlas.ingest.protocol;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
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
public class ProtocolService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProtocolRepository protocolRepository;

    public Protocol addProtocolToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Protocol protocol) {
        protocol.addToSubmissionEnvelope(submissionEnvelope);
        return getProtocolRepository().save(protocol);
    }
}
