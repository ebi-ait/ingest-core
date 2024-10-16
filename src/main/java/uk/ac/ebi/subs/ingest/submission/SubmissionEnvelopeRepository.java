package uk.ac.ebi.subs.ingest.submission;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.state.SubmissionState;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface SubmissionEnvelopeRepository extends MongoRepository<SubmissionEnvelope, String> {

  @RestResource(exported = false)
  SubmissionEnvelope findByUuid(Uuid uuid);

  @RestResource(rel = "findByUuid")
  SubmissionEnvelope findByUuidUuid(@Param("uuid") UUID uuid);

  @RestResource(path = "findByUser", rel = "findByUser")
  Page<SubmissionEnvelope> findByUser(@Param(value = "user") String user, Pageable pageable);

  Page<SubmissionEnvelope> findBySubmissionState(
      @Param("submissionState") SubmissionState submissionState, Pageable pageable);

  long countBySubmissionStateAndUser(SubmissionState submissionState, String user);
}
