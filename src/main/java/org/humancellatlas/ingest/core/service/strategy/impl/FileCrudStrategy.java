package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FileCrudStrategy implements MetadataCrudStrategy<File> {
    private final @NonNull
    FileService fileService;

    @Override
    public File saveMetadataDocument(File document) {
        return fileService.getFileRepository().save(document);
    }

    @Override
    public File findMetadataDocument(String id) {
        return fileService.getFileRepository().findById(id).get();
    }
}