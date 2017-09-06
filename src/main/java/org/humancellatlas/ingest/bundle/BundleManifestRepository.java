package org.humancellatlas.ingest.bundle;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by rolando on 05/09/2017.
 */
public interface BundleManifestRepository extends MongoRepository<BundleManifest, String> {
    BundleManifest findByBundleUuid(Uuid uuid);
}
