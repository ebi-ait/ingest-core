package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface ProtocolRepository extends MongoRepository<Protocol, String> {

    public Page<Protocol> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    @RestResource(exported = false)
    public List<Protocol> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope);


    @RestResource(rel = "findBySubmissionAndValidationState")
    public Page<Protocol> findBySubmissionEnvelopesContainingAndValidationState(@Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
                                                                                @Param("state") ValidationState state,
                                                                                Pageable pageable);

    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Protocol> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<Protocol> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);
}
