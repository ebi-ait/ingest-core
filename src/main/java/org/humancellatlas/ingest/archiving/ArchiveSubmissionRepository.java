package org.humancellatlas.ingest.archiving;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ArchiveSubmissionRepository extends MongoRepository<ArchiveSubmission, String> {
    ArchiveSubmission findByDspUuid(String dspUuid);

}
