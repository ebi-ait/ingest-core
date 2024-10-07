package uk.ac.ebi.subs.ingest.biomaterial;

import java.util.Collection;
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
public interface BiomaterialRepository extends MongoRepository<Biomaterial, String> {

  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Biomaterial> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Biomaterial> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  Page<Biomaterial> findBySubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Pageable pageable);

  Page<Biomaterial> findByProject(Project project, Pageable pageable);

  @RestResource(exported = false)
  Stream<Biomaterial> findByProject(Project project);

  @RestResource(exported = false)
  Stream<Biomaterial> findByProjectsContaining(Project project);

  @RestResource(exported = false)
  Stream<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Collection<Biomaterial> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(rel = "findBySubmissionAndValidationState")
  public Page<Biomaterial> findBySubmissionEnvelopeAndValidationState(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
      @Param("state") ValidationState state,
      Pageable pageable);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }")
  @RestResource(rel = "findBySubmissionIdWithGraphValidationErrors")
  public Page<Biomaterial> findBySubmissionIdWithGraphValidationErrors(
      @Param("envelopeId") String envelopeId, Pageable pageable);

  @RestResource(exported = false)
  Stream<Biomaterial> findByInputToProcessesContains(Process process);

  Page<Biomaterial> findByInputToProcessesContaining(Process process, Pageable pageable);

  @RestResource(exported = false)
  Stream<Biomaterial> findByDerivedByProcessesContains(Process process);

  Page<Biomaterial> findByDerivedByProcessesContaining(Process process, Pageable pageable);

  long countBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  long countBySubmissionEnvelopeAndValidationState(
      SubmissionEnvelope submissionEnvelope, ValidationState validationState);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }",
      count = true)
  long countBySubmissionEnvelopeAndCountWithGraphValidationErrors(String submissionEnvelopeId);
}