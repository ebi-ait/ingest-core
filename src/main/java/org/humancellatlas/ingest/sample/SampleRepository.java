package org.humancellatlas.ingest.sample;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface SampleRepository extends MongoRepository<Sample, String> {
    public Sample findByUuid(Uuid uuid);
}
