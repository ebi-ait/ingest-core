package org.humancellatlas.ingest.envelope;

import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface SubmissionEnvelopeRepository extends MongoRepository<SubmissionEnvelope, String> {
    public SubmissionEnvelope findByUuid(UUID uuid);
}
