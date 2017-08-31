package org.humancellatlas.ingest.submission;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AssayRepository extends MongoRepository<Assay, String> {
    public Assay findByUUID(UUID uuid);
}
