package org.humancellatlas.ingest.project;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface ProjectRepository extends MongoRepository<Project, String> {

  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Project> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Project> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  @RestResource(path = "findByUser", rel = "findByUser")
  Page<Project> findByUser(@Param(value = "user") String user, Pageable pageable);

  @RestResource(rel = "findByUserAndPrimaryWrangler")
  Page<Project> findByUserOrPrimaryWrangler(
      @Param(value = "user") String user,
      @Param(value = "primaryWrangler") String primaryWrangler,
      Pageable pageable);

  Page<Project> findBySubmissionEnvelopesContaining(
      SubmissionEnvelope submissionEnvelope, Pageable pageable);

  @RestResource(exported = false)
  Stream<Project> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(rel = "findBySubmissionAndValidationState")
  public Page<Project> findBySubmissionEnvelopeAndValidationState(
      @Param("envelopeUri") SubmissionEnvelope submissionEnvelope,
      @Param("state") ValidationState state,
      Pageable pageable);

  long countByUser(String user);

  @RestResource(exported = false)
  Collection<Project> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Stream<Project> findBySupplementaryFilesContains(File file);

  @RestResource(exported = false)
  Stream<Project> findBySubmissionEnvelopesContains(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Stream<Project> findByUuid(Uuid uuid);

  @RestResource(rel = "catalogue", path = "catalogue")
  Page<Project> findByIsInCatalogueTrue(Pageable pageable);
}
