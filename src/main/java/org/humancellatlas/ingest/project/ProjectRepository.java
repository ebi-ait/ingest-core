package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface ProjectRepository extends MongoRepository<Project, String> {
    public Project findByUuid(@Param("uuid") Uuid uuid);

    public Page<Project> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Pageable pageable);
}
