package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.bundle.BundleManifest;
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

@CrossOrigin
public interface ProcessRepository extends MongoRepository<Process, String> {

    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Process> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<Process> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

    @RestResource(exported = false)
    List<Process> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope);

    Page<Process> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope,
            Pageable pageable);

    @RestResource(exported = false)
    Page<Process> findByInputBundleManifestsContaining(BundleManifest bundleManifest, Pageable pageable);

    @RestResource(rel = "findBySubmissionAndValidationState")
    public Page<Process> findBySubmissionEnvelopesContainingAndValidationState(@Param
            ("envelopeUri") SubmissionEnvelope submissionEnvelope, @Param("state")
            ValidationState state, Pageable pageable);

}
