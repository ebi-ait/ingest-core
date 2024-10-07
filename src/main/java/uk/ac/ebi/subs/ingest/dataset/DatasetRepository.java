package uk.ac.ebi.subs.ingest.dataset;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

/** Javadocs go here! */
@CrossOrigin
public interface DatasetRepository extends MongoRepository<Dataset, String> {
  @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
  Page<Dataset> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

  @RestResource(exported = false)
  Optional<Dataset> findByUuid(Uuid uuid);

  @RestResource(rel = "findByUuid", path = "findByUuid")
  Optional<Dataset> findByUuidUuidAndIsUpdateFalse(@Param("uuid") UUID uuid);

  Page<Dataset> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
