package uk.ac.ebi.subs.ingest.study;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@CrossOrigin
public interface StudyRepository extends MongoRepository<Study, String> {

  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Study> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(exported = false)
  Stream<Study> findByUuid(Uuid uuid);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Study> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  Page<Study> findBySubmissionEnvelopesContaining(
      SubmissionEnvelope submissionEnvelope, Pageable pageable);

  @RestResource(exported = false)
  Stream<Study> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);

  @RestResource(exported = false)
  Collection<Study> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);
}
