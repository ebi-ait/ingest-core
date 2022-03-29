package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProcessCrudStrategy implements MetadataCrudStrategy<Process> {
    private final @NonNull ProcessRepository processRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull BiomaterialRepository biomaterialRepository;

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
    public Stream<Process> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return processRepository.findBySubmissionEnvelope(submissionEnvelope);
    }

    @Override
    public Collection<Process> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return processRepository.findAllBySubmissionEnvelope(submissionEnvelope);
    }

    @Override
    public void unlinkAndDeleteDocument(Process document) {
        document.setValidationState(ValidationState.VALID);
        fileRepository.findByInputToProcessesContains(document).forEach(file -> {
            file.getInputToProcesses().remove(document);
            fileRepository.save(file);
        });
        fileRepository.findByDerivedByProcessesContains(document).forEach(file -> {
            file.getDerivedByProcesses().remove(document);
            fileRepository.save(file);
        });
        biomaterialRepository.findByInputToProcessesContains(document).forEach(biomaterial -> {
            biomaterial.getInputToProcesses().remove(document);
            biomaterialRepository.save(biomaterial);
        });
        biomaterialRepository.findByDerivedByProcessesContains(document).forEach(biomaterial -> {
            biomaterial.getDerivedByProcesses().remove(document);
            biomaterialRepository.save(biomaterial);
        });
        processRepository.delete(document);
    }
}