package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


/**
 * Created by rolando on 16/02/2018.
 */
public interface BiomaterialRepository extends MongoRepository<Biomaterial, String> {

  public Page<Biomaterial> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

  public Page<Biomaterial> findBySubmissionEnvelopesContainingAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState state, Pageable pageable);

  public List<Biomaterial> findByInputToProcessesContaining(Process process);

  public List<Biomaterial> findByDerivedByProcessesContaining(Process process);
}

