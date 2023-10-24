package org.humancellatlas.ingest.file;


import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.security.RowLevelFilterSecurity;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by rolando on 06/09/2017.
 */
@CrossOrigin
@RowLevelFilterSecurity(expression ="#authentication.authorities.contains(" +
        "new org.springframework.security.core.authority.SimpleGrantedAuthority(" +
        "'ROLE_access_' +#filterObject.project.uuid.toString())) " +
        "or #filterObject.project.dataAccess eq T(org.humancellatlas.ingest.project.DataAccessTypes).OPEN",
        ignoreClasses = {Project.class})
public interface FileRepository extends MongoRepository<File, String> {


    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<File> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(rel = "findByUuid", path = "findByUuid")
    Optional<File> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

    Page<File> findByProject(Project project, Pageable pageable);

    @RestResource(exported = false)
    Stream<File> findByProject(Project project);

    @RestResource(rel = "findBySubmissionEnvelope")
    Page<File> findBySubmissionEnvelope(@Param("envelopeUri") SubmissionEnvelope submissionEnvelope, Pageable pageable);

    @RestResource(exported = false)
    Stream<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    List<File> findBySubmissionEnvelopeAndFileName(SubmissionEnvelope submissionEnvelope, String fileName);


    @RestResource(rel = "findBySubmissionAndValidationState")
    public Page<File> findBySubmissionEnvelopeAndValidationState(@Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
                                                                 @Param("state") ValidationState state,
                                                                 Pageable pageable);

    @Query(value = "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }")
    @RestResource(rel = "findBySubmissionIdWithGraphValidationErrors")
    public Page<File> findBySubmissionIdWithGraphValidationErrors(
            @Param("envelopeId") String envelopeId,
            Pageable pageable
    );

    @RestResource(exported = false)
    Collection<File> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @RestResource(exported = false)
    Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @RestResource(rel = "findByValidationId")
    File findByValidationJobValidationId(@Param("validationId") UUID id);

    @RestResource(exported = false)
    Stream<File> findByInputToProcessesContains(Process process);

    Page<File> findByInputToProcessesContaining(Process process, Pageable pageable);

    @RestResource(exported = false)
    Stream<File> findByDerivedByProcessesContains(Process process);

    Page<File> findByDerivedByProcessesContaining(Process process, Pageable pageable);

    long countBySubmissionEnvelopeAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState validationState);

    long countBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

    @Query(value = "{'submissionEnvelope.id': ?0, validationErrors: {$elemMatch: {errorType: ?1} }}", count = true)
    long countBySubmissionEnvelopeIdAndErrorType(@Param("id") String submissionEnvelopeId, @Param("errorType") String errorType);

    @Query(value = "{'submissionEnvelope.id': ?0, validationErrors: {$elemMatch: {errorType: ?1} }}")
    Page<File> findBySubmissionEnvelopeIdAndErrorType(@Param("id") String submissionEnvelopeId, @Param("errorType") String errorType, Pageable pageable);

    @Query(value = "{'submissionEnvelope.id': ?0, validationErrors: {$not: {$elemMatch: {errorType: ?1} }}}", count = true)
    long countBySubmissionEnvelopeIdAndNotErrorType(@Param("id") String submissionEnvelopeId, @Param("errorType") String errorType);

    @Query(value = "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }", count = true)
    long countBySubmissionEnvelopeAndCountWithGraphValidationErrors(String submissionEnvelopeId);
}
