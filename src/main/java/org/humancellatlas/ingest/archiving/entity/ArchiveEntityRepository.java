package org.humancellatlas.ingest.archiving.entity;

import org.humancellatlas.ingest.archiving.submission.ArchiveSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ArchiveEntityRepository extends MongoRepository<ArchiveEntity, String> {
    Page<ArchiveEntity> findByArchiveSubmission(ArchiveSubmission archiveSubmission,
                                                Pageable pageable);

    Page<ArchiveEntity> findByAlias(String alias,
                                    Pageable pageable);

    ArchiveEntity findByDspUuid(String dspUuid);

    Page<ArchiveEntity> findByArchiveSubmissionAndType(ArchiveSubmission archiveSubmission,
                                                       ArchiveEntityType archiveEntityType,
                                                       Pageable pageable);

}
