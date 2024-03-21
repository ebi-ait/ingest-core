package org.humancellatlas.ingest.dataset;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.study.Study;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;
import java.util.UUID;

/**
 * Javadocs go here!
 */
@CrossOrigin
public interface DatasetRepository extends MongoRepository<Dataset, String> {
    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Dataset> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(exported = false)
    Optional<Dataset> findByUuid(Uuid uuid);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<Dataset> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

    Page<Dataset> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
