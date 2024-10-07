package uk.ac.ebi.subs.ingest.archiving.submission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ArchiveSubmissionRepository extends MongoRepository<ArchiveSubmission, String> {
  ArchiveSubmission findByDspUuid(String dspUuid);

  Page<ArchiveSubmission> findBySubmissionUuid(String submissionUuid, Pageable pageable);
}
