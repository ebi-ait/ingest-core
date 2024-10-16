package uk.ac.ebi.subs.ingest.file;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.security.RowLevelFilterSecurity;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

/** Created by rolando on 06/09/2017. */
@CrossOrigin
@RowLevelFilterSecurity(
    expression =
        "(#filterObject.project != null)"
            + "? "
            + "   ("
            + "      #authentication.authorities.![authority].contains("
            + "          'ROLE_access_' +#filterObject.project.uuid?.toString()) "
            + "     or "
            + "      #authentication.authorities.![authority].contains('ROLE_SERVICE') "
            + "     or "
            + "      #filterObject.project.content['dataAccess']['type'] "
            + "         eq T(uk.ac.ebi.subs.ingest.project.DataAccessTypes).OPEN.label"
            + "   )"
            + ":true",
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
  Page<File> findBySubmissionEnvelope(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope, Pageable pageable);

  @RestResource(exported = false)
  Stream<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  List<File> findBySubmissionEnvelopeAndFileName(
      SubmissionEnvelope submissionEnvelope, String fileName);

  @RestResource(rel = "findBySubmissionAndValidationState")
  public Page<File> findBySubmissionEnvelopeAndValidationState(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
      @Param("state") ValidationState state,
      Pageable pageable);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }")
  @RestResource(rel = "findBySubmissionIdWithGraphValidationErrors")
  public Page<File> findBySubmissionIdWithGraphValidationErrors(
      @Param("envelopeId") String envelopeId, Pageable pageable);

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

  long countBySubmissionEnvelopeAndValidationState(
      SubmissionEnvelope submissionEnvelope, ValidationState validationState);

  long countBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @Query(
      value = "{'submissionEnvelope.id': ?0, validationErrors: {$elemMatch: {errorType: ?1} }}",
      count = true)
  long countBySubmissionEnvelopeIdAndErrorType(
      @Param("id") String submissionEnvelopeId, @Param("errorType") String errorType);

  @Query(value = "{'submissionEnvelope.id': ?0, validationErrors: {$elemMatch: {errorType: ?1} }}")
  Page<File> findBySubmissionEnvelopeIdAndErrorType(
      @Param("id") String submissionEnvelopeId,
      @Param("errorType") String errorType,
      Pageable pageable);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, validationErrors: {$not: {$elemMatch: {errorType: ?1} }}}",
      count = true)
  long countBySubmissionEnvelopeIdAndNotErrorType(
      @Param("id") String submissionEnvelopeId, @Param("errorType") String errorType);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }",
      count = true)
  long countBySubmissionEnvelopeAndCountWithGraphValidationErrors(String submissionEnvelopeId);
}
