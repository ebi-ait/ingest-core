package org.humancellatlas.ingest.biomaterial;

import java.util.List;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by rolando on 16/02/2018.
 */
public interface BiomaterialRepository extends MongoRepository<Biomaterial, String> {

  Page<Biomaterial> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

}
