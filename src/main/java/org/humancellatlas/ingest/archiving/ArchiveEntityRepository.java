package org.humancellatlas.ingest.archiving;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ArchiveEntityRepository extends MongoRepository<ArchiveEntity, String> {
    Page<ArchiveEntity> findByArchiveSubmission(ArchiveSubmission archiveSubmission,
                                                Pageable pageable);

    Page<ArchiveEntity> findByArchiveSubmissionAndType(ArchiveSubmission archiveSubmission,
                                                       ArchiveEntityType archiveEntityType,
                                                       Pageable pageable);
}
