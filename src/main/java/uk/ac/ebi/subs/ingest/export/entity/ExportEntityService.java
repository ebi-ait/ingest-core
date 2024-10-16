package uk.ac.ebi.subs.ingest.export.entity;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import uk.ac.ebi.subs.ingest.export.entity.web.ExportEntityRequest;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;

@Component
@AllArgsConstructor
public class ExportEntityService {
  private final ExportEntityRepository exportEntityRepository;

  public ExportEntity createExportEntity(
      ExportJob exportJob, ExportEntityRequest exportEntityRequest) {
    ExportEntity newExportEntity =
        ExportEntity.builder()
            .exportJob(exportJob)
            .status(exportEntityRequest.getStatus())
            .context(exportEntityRequest.getContext())
            .errors(exportEntityRequest.getErrors())
            .build();
    return exportEntityRepository.insert(newExportEntity);
  }
}
