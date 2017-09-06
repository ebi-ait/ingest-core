package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by rolando on 06/09/2017.
 */
public interface FileRepository extends MongoRepository<File, String> {
    public File findByUuid(Uuid uuid);
}
