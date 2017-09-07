package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by rolando on 06/09/2017.
 */
public interface FileRepository extends MongoRepository<File, String> {
    public File findByUuid(Uuid uuid);

    public List<File> findBySubmissionEnvelopeUuid(Uuid uuid);
}
