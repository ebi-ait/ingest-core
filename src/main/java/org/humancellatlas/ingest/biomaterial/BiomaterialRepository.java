package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@CrossOrigin
public interface BiomaterialRepository extends MongoRepository<Biomaterial, String> {

    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Biomaterial> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<Biomaterial> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

    Page<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    Page<Biomaterial> findByProject(Project project, Pageable pageable);

    @RestResource(exported = false)
    Stream<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @RestResource(exported = false)
    Collection<Biomaterial> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @RestResource(exported = false)
    Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @RestResource(rel = "findBySubmissionAndValidationState")
    public Page<Biomaterial> findBySubmissionEnvelopeAndValidationState(@Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
                                                                        @Param("state") ValidationState state,
                                                                        Pageable pageable);

    @RestResource(exported = false)
    Stream<Biomaterial> findByInputToProcessesContains(Process process);

    Page<Biomaterial> findByInputToProcessesContaining(Process process, Pageable pageable);

    @RestResource(exported = false)
    Stream<Biomaterial> findByDerivedByProcessesContains(Process process);

    Page<Biomaterial> findByDerivedByProcessesContaining(Process process, Pageable pageable);

}
