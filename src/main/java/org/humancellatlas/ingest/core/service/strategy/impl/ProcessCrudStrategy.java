package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProcessCrudStrategy implements MetadataCrudStrategy<Process> {
    private final @NonNull
    ProcessService processService;

    @Override
    public Process saveMetadataDocument(Process document) {
        return processService.getProcessRepository().save(document);
    }

    @Override
    public Process findMetadataDocument(String id) {
        return processService.getProcessRepository().findById(id).get();
    }
}