package org.humancellatlas.ingest.analysis;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    public Analysis findByUuid(UUID uuid);
}
