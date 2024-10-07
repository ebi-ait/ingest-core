package uk.ac.ebi.subs.ingest.protocol;

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
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.security.RowLevelFilterSecurity;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
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
public interface ProtocolRepository extends MongoRepository<Protocol, String> {

  public Page<Protocol> findBySubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Pageable pageable);

  public Page<Protocol> findByProject(Project project, Pageable pageable);

  @RestResource(exported = false)
  Stream<Protocol> findByProject(Project project);

  @RestResource(exported = false)
  public Stream<Protocol> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(rel = "findBySubmissionAndValidationState")
  public Page<Protocol> findBySubmissionEnvelopeAndValidationState(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
      @Param("state") ValidationState state,
      Pageable pageable);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }")
  @RestResource(rel = "findBySubmissionIdWithGraphValidationErrors")
  public Page<Protocol> findBySubmissionIdWithGraphValidationErrors(
      @Param("envelopeId") String envelopeId, Pageable pageable);

  @RestResource(exported = false)
  Collection<Protocol> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Protocol> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Protocol> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  long countBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  long countBySubmissionEnvelopeAndValidationState(
      SubmissionEnvelope submissionEnvelope, ValidationState validationState);

  @Query(
      value =
          "{'submissionEnvelope.id': ?0, graphValidationErrors: { $exists: true, $not: {$size: 0} } }",
      count = true)
  long countBySubmissionEnvelopeAndCountWithGraphValidationErrors(String submissionEnvelopeId);
}