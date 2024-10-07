package uk.ac.ebi.subs.ingest.process;

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

import uk.ac.ebi.subs.ingest.bundle.BundleManifest;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
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
public interface ProcessRepository extends MongoRepository<Process, String> {

  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Process> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Process> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  Page<Process> findByProject(Project project, Pageable pageable);

  @RestResource(exported = false)
  Stream<Process> findByProject(Project project);

  @RestResource(exported = false)
  Stream<Process> findByProjectsContaining(Project project);

  Page<Process> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);

  @RestResource(exported = false)
  Stream<Process> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(rel = "findBySubmissionAndValidationState")
  public Page<Process> findBySubmissionEnvelopeAndValidationState(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
      @Param("state") ValidationState state,
      Pageable pageable);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }")
  @RestResource(rel = "findBySubmissionIdWithGraphValidationErrors")
  public Page<Process> findBySubmissionIdWithGraphValidationErrors(
      @Param("envelopeId") String envelopeId, Pageable pageable);

  @RestResource(exported = false)
  Collection<Process> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Page<Process> findByInputBundleManifestsContaining(
      BundleManifest bundleManifest, Pageable pageable);

  @RestResource(exported = false)
  public Stream<Process> findAllByIdIn(Collection<String> ids);

  @RestResource(exported = false)
  Stream<Process> findByProtocolsContains(Protocol protocol);

  Stream<Process> findByInputBundleManifestsContains(BundleManifest bundleManifest);

  @RestResource(exported = false)
  Optional<Process> findFirstByProtocolsContains(Protocol protocol);

  long countBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  long countBySubmissionEnvelopeAndValidationState(
      SubmissionEnvelope submissionEnvelope, ValidationState validationState);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }",
      count = true)
  long countBySubmissionEnvelopeAndCountWithGraphValidationErrors(String submissionEnvelopeId);
}