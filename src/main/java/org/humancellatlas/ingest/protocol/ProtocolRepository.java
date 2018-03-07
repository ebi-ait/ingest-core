package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.ValidationState;
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
public interface ProtocolRepository extends MongoRepository<Protocol, String> {
    public Protocol findByUuid(@Param("uuid") Uuid uuid);

    public Page<Protocol> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    public Page<Protocol> findBySubmissionEnvelopesContainingAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState state, Pageable pageable);
}
