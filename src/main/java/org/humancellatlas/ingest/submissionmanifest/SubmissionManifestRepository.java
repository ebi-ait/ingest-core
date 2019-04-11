package org.humancellatlas.ingest.submissionmanifest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Created by rolando on 30/05/2018.
 */
@CrossOrigin
public interface SubmissionManifestRepository extends MongoRepository<SubmissionManifest, String> {
    <S extends SubmissionManifest> S findBySubmissionEnvelopeId(String envelopeId);
}
