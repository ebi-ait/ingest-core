package org.humancellatlas.ingest.archiving.entity;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ArchiveJobRepository extends MongoRepository<ArchiveJob, String> {

    Page<ArchiveJob> findBySubmissionUuid(String submissionUuid, Pageable pageable);
}
