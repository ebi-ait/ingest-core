package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.springframework.stereotype.Component;

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
        return processRepository.findOne(id);
    }

    @Override
    public Process findOriginalByUuid(String uuid) {
        return processRepository.findByUuidAndIsUpdateFalse(new Uuid(uuid));
    }
}