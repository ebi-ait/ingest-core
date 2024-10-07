package uk.ac.ebi.subs.ingest.bundle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

public class BundleManifestRepositoryImpl implements BundleManifestRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public BundleManifestRepositoryImpl(final MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Page<BundleManifest> findBundleManifestsByProjectAndBundleType(
      Project project, BundleType bundleType, Pageable pageable) {
    SubmissionEnvelope submissionEnvelope = project.getSubmissionEnvelope();
    String submissionUuid = submissionEnvelope.getUuid().getUuid().toString();
    String projectUuid = project.getUuid().getUuid().toString();

    Query query = new Query();
    query.addCriteria(Criteria.where("fileProjectMap." + projectUuid).exists(true));

    if (bundleType != null) {
      if (bundleType.equals(BundleType.PRIMARY)) {
        query.addCriteria(Criteria.where("envelopeUuid").is(submissionUuid));
      } else if (bundleType.equals(BundleType.ANALYSIS)) {
        // TODO This might not be the best criteria to query analysis bundles. Might need to remodel
        // bundle manifest.
        query.addCriteria(Criteria.where("envelopeUuid").ne(submissionUuid));
      }
    }

    query.with(pageable);

    List<BundleManifest> result = mongoTemplate.find(query, BundleManifest.class);
    long count = mongoTemplate.count(query, BundleManifest.class);
    Page<BundleManifest> bundleManifestPage = new PageImpl<>(result, pageable, count);
    return bundleManifestPage;
  }
}
