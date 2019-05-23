package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@AllArgsConstructor
public class ProtocolCrudStrategy implements MetadataCrudStrategy<Protocol> {
    private final @NonNull ProtocolRepository protocolRepository;

    @Override
    public Protocol saveMetadataDocument(Protocol document) {
        return protocolRepository.save(document);
    }

    @Override
    public Protocol findMetadataDocument(String id) {
        return protocolRepository.findOne(id);
    }

    @Override
    public Protocol findOriginalByUuid(String uuid) {
        return protocolRepository.findByUuidAndIsUpdateFalseOrIsUpdateNull(new Uuid(uuid));
    }

    @Override
    public Collection<Protocol> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return protocolRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
    }
}