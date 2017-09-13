package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by rolando on 06/09/2017.
 */
public interface FileRepository extends MongoRepository<File, String> {
    public File findByUuid(@Param("uuid") Uuid uuid);

    public Page<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    public List<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);
}
