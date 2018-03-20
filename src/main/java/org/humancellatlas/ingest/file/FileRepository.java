package org.humancellatlas.ingest.file;


import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.UUID;

/**
 * Created by rolando on 06/09/2017.
 */
@CrossOrigin
public interface FileRepository extends MongoRepository<File, String> {

    File findByUuid(@Param("uuid") Uuid uuid);

    @RestResource(exported = false)
    List<File> findBySubmissionEnvelopesContains(SubmissionEnvelope submissionEnvelope);

    Page<File> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    List<File> findBySubmissionEnvelopesInAndFileName(SubmissionEnvelope submissionEnvelope, String fileName);

    File findByValidationId(@Param("validationId") UUID id);

    @RestResource(exported = false)
    List<File> findByInputToProcessesContains(Process process);

    Page<File> findByInputToProcessesContaining(Process process, Pageable pageable);

    @RestResource(exported = false)
    List<File> findByDerivedByProcessesContains(Process process);

    Page<File> findByDerivedByProcessesContaining(Process process, Pageable pageable);

    @RestResource(exported = false)
    public Page<File> findBySubmissionEnvelopesContainingAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState state, Pageable pageable);
}