package uk.ac.ebi.subs.ingest.gene;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GeneRepository extends MongoRepository<Gene, String> {
    Gene findByHgncId(String hgncId);
    Gene findByName(String name);
}
