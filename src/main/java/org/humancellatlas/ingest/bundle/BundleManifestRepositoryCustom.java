package org.humancellatlas.ingest.bundle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BundleManifestRepositoryCustom {
    Page<BundleManifest> findBundles(String projectUuid, String primarySubmissionUuid, Boolean isPrimary, Pageable pageable);
}
