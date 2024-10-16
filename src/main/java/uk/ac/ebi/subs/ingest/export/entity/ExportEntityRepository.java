package uk.ac.ebi.subs.ingest.export.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.export.ExportState;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;

@CrossOrigin
@RestResource(exported = false)
public interface ExportEntityRepository extends MongoRepository<ExportEntity, String> {

  Page<ExportEntity> findByExportJob(ExportJob exportJob, Pageable pageable);

  Page<ExportEntity> findByExportJobAndStatus(
      ExportJob exportJob, ExportState exportState, Pageable pageable);
}
