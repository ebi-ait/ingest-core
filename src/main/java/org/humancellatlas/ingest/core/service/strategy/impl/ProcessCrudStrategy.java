package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ProcessCrudStrategy implements MetadataCrudStrategy<Process> {
    private final @NonNull ProcessRepository processRepository;

    @Override
    public Process saveMetadataDocument(Process document) {
        return processRepository.save(document);
    }

    @Override
    public Process findMetadataDocument(String id) {
        return processRepository.findById(id)
                                .orElseThrow(() -> {
                                    throw new ResourceNotFoundException();
                                });
    }

    @Override
    public Process findOriginalByUuid(String uuid) {
        return processRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
                                .orElseThrow(() -> {
                                    throw new ResourceNotFoundException();
                                });
    }

    @Override
    public Collection<Process> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return processRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
    }
}