package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@CrossOrigin
public interface SubmissionEnvelopeRepository extends MongoRepository<SubmissionEnvelope, String> {

    @RestResource(path="findByUuid", rel="findByUuid")
    SubmissionEnvelope findByUuid(Uuid uuid);

    @RestResource(path = "findByUser", rel = "findByUser")
    Page<SubmissionEnvelope> findByUser(@Param(value = "user") String user, Pageable pageable);

    long countBySubmissionState(SubmissionState submissionState);

    long countBySubmissionStateAndUser(SubmissionState submissionState, String user);

}
