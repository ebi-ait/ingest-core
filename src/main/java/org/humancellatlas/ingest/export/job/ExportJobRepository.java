package org.humancellatlas.ingest.export.job;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ExportJobRepository extends MongoRepository<ExportJob, String> {
    Page<ExportJob> findBySubmission(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
