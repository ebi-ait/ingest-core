package org.humancellatlas.ingest.analysis;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    public Analysis findByUuid(@Param("uuid") Uuid uuid);

    public Page<Analysis> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
