package org.humancellatlas.ingest.export.job;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ExportJobRepository extends MongoRepository<ExportJob, String> {

}
