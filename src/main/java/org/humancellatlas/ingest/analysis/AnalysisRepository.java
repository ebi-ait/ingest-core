package org.humancellatlas.ingest.analysis;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    public Analysis findByUuid(@Param("uuid") Uuid uuid);

    public Page<Analysis> findBySubmissionEnvelopesIn(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
