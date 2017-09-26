package org.humancellatlas.ingest.bundle;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Created by rolando on 05/09/2017.
 */
@CrossOrigin
public interface BundleManifestRepository extends MongoRepository<BundleManifest, String> {
    BundleManifest findByBundleUuid(String uuid);
}
