package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.query.MetadataCriteria;
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
public interface ProjectRepository extends MongoRepository<Project, String> , ProjectRepositoryCustom{

    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Project> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<Project> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

    @RestResource(path = "findByUser", rel = "findByUser")
    Page<Project> findByUser(@Param(value = "user") String user, Pageable pageable);

    Page<Project> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    @RestResource(exported = false)
    List<Project> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope);

    @RestResource(rel = "findBySubmissionAndValidationState")
    public Page<Project> findBySubmissionEnvelopesContainingAndValidationState(@Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
                                                                               @Param("state") ValidationState state,
                                                                               Pageable pageable);

    long countByUser(String user);
    
    
    Page<Project> findByContent(List<MetadataCriteria> criteria, Pageable pageable);
}
