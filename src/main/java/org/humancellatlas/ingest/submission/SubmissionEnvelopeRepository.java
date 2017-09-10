package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface SubmissionEnvelopeRepository extends MongoRepository<SubmissionEnvelope, String> {
    public SubmissionEnvelope findByUuid(Uuid uuid);
}
