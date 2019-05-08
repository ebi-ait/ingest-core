package org.humancellatlas.ingest.patch;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface PatchRepository extends MongoRepository<Patch, String> {

}
