package org.humancellatlas.ingest.analysis;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    public Analysis findByUuid(Uuid uuid);
}
