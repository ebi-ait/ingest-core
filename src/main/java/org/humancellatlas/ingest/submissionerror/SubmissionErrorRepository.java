package org.humancellatlas.ingest.submissionerror;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface SubmissionErrorRepository  extends MongoRepository<SubmissionError, String> {

    Page<SubmissionError> findBySubmissionEnvelopeId(String envelopeId, Pageable pageable);
}
