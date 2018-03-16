package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
public interface ProcessRepository extends MongoRepository<Process, String> {

    @RestResource(exported = false)
    List<Process> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope);

    Page<Process> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    Page<Process> findBySubmissionEnvelopesContainingAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState state, Pageable pageable);
}
