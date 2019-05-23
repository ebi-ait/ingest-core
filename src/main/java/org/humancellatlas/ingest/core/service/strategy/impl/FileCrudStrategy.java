package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
        return fileRepository.findOne(id);
    }

    @Override
    public File findOriginalByUuid(String uuid) {
        return fileRepository.findByUuidAndIsUpdateFalseOrIsUpdateNull(new Uuid(uuid));
    }

    @Override
    public Collection<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return fileRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
    }
}