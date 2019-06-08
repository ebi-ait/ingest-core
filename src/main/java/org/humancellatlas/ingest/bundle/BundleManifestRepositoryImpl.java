package org.humancellatlas.ingest.bundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class BundleManifestRepositoryImpl implements BundleManifestRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public BundleManifestRepositoryImpl(final MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<BundleManifest> findBundles(String projectUuid, String primarySubmissionUuid, Boolean isPrimary, Pageable pageable) {
        Query query = new Query();

        query.addCriteria(Criteria.where("fileProjectMap." + projectUuid).exists(true));

        if (isPrimary != null && isPrimary){
            query.addCriteria(Criteria.where("envelopeUuid").is(primarySubmissionUuid));
        }

        if(isPrimary !=null && !isPrimary){
            // TODO This might not be the best criteria to query analysis bundles. Might need to remodel bundle manifest.
            query.addCriteria(Criteria.where("envelopeUuid").ne(primarySubmissionUuid));
        }

        query.with(pageable);

        List<BundleManifest> result = mongoTemplate.find(query, BundleManifest.class);
        long count = mongoTemplate.count(query, BundleManifest.class);
        Page<BundleManifest> bundleManifestPage = new PageImpl<>(result, pageable, count);
        return bundleManifestPage;
    }
}
