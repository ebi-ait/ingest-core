package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class FileCrudStrategy implements MetadataCrudStrategy<File> {
    private final @NonNull FileRepository fileRepository;

    @Override
    public File saveMetadataDocument(File document) {
        return fileRepository.save(document);
    }

    @Override
    public File findMetadataDocument(String id) {
        return fileRepository.findById(id)
                             .orElseThrow(() -> {
                                 throw new ResourceNotFoundException();
                             });
    }

    @Override
    public File findOriginalByUuid(String uuid) {
        return fileRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
                             .orElseThrow(() -> {
                                 throw new ResourceNotFoundException();
                             });
    }

    @Override
    public Stream<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return fileRepository.findBySubmissionEnvelope(submissionEnvelope);
    }
}