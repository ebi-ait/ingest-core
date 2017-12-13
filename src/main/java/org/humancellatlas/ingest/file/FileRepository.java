package org.humancellatlas.ingest.file;

import java.util.UUID;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * Created by rolando on 06/09/2017.
 */
@CrossOrigin
public interface FileRepository extends MongoRepository<File, String> {
    public File findByUuid(@Param("uuid") Uuid uuid);

    public Page<File> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    public List<File> findBySubmissionEnvelopesInAndFileName(SubmissionEnvelope submissionEnvelope, String fileName);

    public File findByValidationId(@Param("validationId") UUID id);
}
