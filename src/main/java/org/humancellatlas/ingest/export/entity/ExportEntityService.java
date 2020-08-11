package org.humancellatlas.ingest.export.entity;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.entity.web.ExportEntityRequest;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExportEntityService {
    private final ExportEntityRepository exportEntityRepository;

    public ExportEntity createExportEntity(ExportJob exportJob, ExportEntityRequest exportEntityRequest) {
        ExportEntity newExportEntity = ExportEntity.builder()
            .exportJob(exportJob)
            .status(exportEntityRequest.getStatus())
            .context(exportEntityRequest.getContext())
            .errors(exportEntityRequest.getErrors())
            .build();
        return exportEntityRepository.insert(newExportEntity);
    }
}
