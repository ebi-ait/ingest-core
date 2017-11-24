package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface SubmissionEnvelopeRepository extends MongoRepository<SubmissionEnvelope, String> {
    SubmissionEnvelope findByUuid(Uuid uuid);

    List<SubmissionEnvelope> findBySubmissionState(SubmissionState submissionState);
}
