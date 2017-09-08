package org.humancellatlas.ingest.assay;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AssayRepository extends MongoRepository<Assay, String> {
    public Assay findByUuid(Uuid uuid);

    public Page<Assay> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
