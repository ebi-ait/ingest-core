package uk.ac.ebi.subs.ingest.export.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@CrossOrigin
public interface ExportJobRepository extends MongoRepository<ExportJob, String> {
  Page<ExportJob> findBySubmission(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
