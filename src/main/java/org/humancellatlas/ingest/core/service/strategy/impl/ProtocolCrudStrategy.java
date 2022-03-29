package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProtocolCrudStrategy implements MetadataCrudStrategy<Protocol> {
    private final @NonNull ProtocolRepository protocolRepository;
    private final @NonNull ProcessRepository processRepository;

    @Override
    public Protocol saveMetadataDocument(Protocol document) {
        return protocolRepository.save(document);
    }

    @Override
    public Protocol findMetadataDocument(String id) {
        return protocolRepository.findById(id)
                                 .orElseThrow(() -> {
                                     throw new ResourceNotFoundException();
                                 });
    }

    @Override
    public Protocol findOriginalByUuid(String uuid) {
        return protocolRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
                                 .orElseThrow(() -> {
                                     throw new ResourceNotFoundException();
                                 });
    }

    @Override
    public Stream<Protocol> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return protocolRepository.findBySubmissionEnvelope(submissionEnvelope);
    }

    @Override
    public Collection<Protocol> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope);
    }

    @Override
    public void unlinkAndDeleteDocument(Protocol document) {
        document.setValidationState(ValidationState.VALID);
        processRepository.findByProtocolsContains(document).forEach(process -> {
            process.getProtocols().remove(document);
            processRepository.save(process);
        });
        protocolRepository.delete(document);
    }
}