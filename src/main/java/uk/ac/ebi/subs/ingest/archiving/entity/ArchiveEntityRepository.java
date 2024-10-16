package uk.ac.ebi.subs.ingest.archiving.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.archiving.submission.ArchiveSubmission;

@CrossOrigin
public interface ArchiveEntityRepository extends MongoRepository<ArchiveEntity, String> {
  Page<ArchiveEntity> findByArchiveSubmission(
      ArchiveSubmission archiveSubmission, Pageable pageable);

  Page<ArchiveEntity> findByAlias(String alias, Pageable pageable);

  ArchiveEntity findByArchiveSubmissionAndAlias(ArchiveSubmission archiveSubmission, String alias);

  ArchiveEntity findByDspUuid(String dspUuid);

  Page<ArchiveEntity> findByArchiveSubmissionAndType(
      ArchiveSubmission archiveSubmission, ArchiveEntityType archiveEntityType, Pageable pageable);

  @RestResource(exported = false)
  Long deleteByArchiveSubmission(ArchiveSubmission archiveSubmission);
}
