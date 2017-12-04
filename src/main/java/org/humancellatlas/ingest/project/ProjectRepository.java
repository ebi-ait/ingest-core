package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
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
public interface ProjectRepository extends MongoRepository<Project, String> {

    Project findByUuid(@Param("uuid") Uuid uuid);

    @RestResource(path = "findByUser", rel = "findByUser")
    Page<Project> findByUser(@Param(value = "user") String user, Pageable pageable);

    Page<Project> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    long countByUser(String user);
}
