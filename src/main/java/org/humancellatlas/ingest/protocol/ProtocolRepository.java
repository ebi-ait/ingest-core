package org.humancellatlas.ingest.protocol;

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
public interface ProtocolRepository extends MongoRepository<Protocol, String> {
    public Protocol findByUuid(@Param("uuid") Uuid uuid);

    public Page<Protocol> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
