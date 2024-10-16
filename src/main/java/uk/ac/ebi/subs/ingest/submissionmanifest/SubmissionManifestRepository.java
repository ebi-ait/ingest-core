package uk.ac.ebi.subs.ingest.submissionmanifest;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

/** Created by rolando on 30/05/2018. */
@CrossOrigin
public interface SubmissionManifestRepository extends MongoRepository<SubmissionManifest, String> {
  <S extends SubmissionManifest> S findBySubmissionEnvelopeId(String envelopeId);

  Long deleteBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope);
}
