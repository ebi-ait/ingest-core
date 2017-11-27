package org.humancellatlas.ingest.protocol;

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
public class ProtocolService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProtocolRepository protocolRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Protocol addProtocolToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Protocol protocol) {
        protocol.addToSubmissionEnvelope(submissionEnvelope);
        return getProtocolRepository().save(protocol);
    }

    public SubmissionEnvelope resolveProtocolReferencesForSubmission(SubmissionEnvelope submissionEnvelope, MetadataReference protocolReference) {
        List<Protocol> protocols = new ArrayList<>();

        for (String protocolUuid : protocolReference.getUuids()) {
            Uuid protocolUuidObj = new Uuid(protocolUuid);
            Protocol protocol = getProtocolRepository().findByUuid(protocolUuidObj);

            if (protocol != null) {
                protocol.addToSubmissionEnvelope(submissionEnvelope);
                protocols.add(protocol);
                getLog().info(String.format("Adding protocol to submission envelope '%s'", protocol.getId()));
            }
            else {
                getLog().warn(String.format(
                        "No Protocol present with UUID '%s' - in future this will cause a critical error",
                        protocolUuid));
            }
        }

        getProtocolRepository().save(protocols);

        return submissionEnvelope;
    }
}
