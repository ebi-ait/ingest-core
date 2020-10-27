package org.humancellatlas.ingest.protocol;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;

    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull ProcessRepository processRepository;


    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Protocol addProtocolToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Protocol protocol) {
        if(! protocol.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(protocol, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(protocol, submissionEnvelope);
        }
    }

    public Page<Protocol> retrieve(SubmissionEnvelope submission, Pageable pageable) {
        Page<Protocol> protocols = protocolRepository.findBySubmissionEnvelope(submission, pageable);
        protocols.forEach(protocol -> {
            processRepository.findOneByProtocolsContains(protocol).ifPresent(it -> protocol.markAsLinked());
        });
        return protocols;
    }

}
