package org.humancellatlas.ingest.assay;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AssayRepository extends MongoRepository<Assay, String> {
    public Assay findByUuid(Uuid uuid);
}
