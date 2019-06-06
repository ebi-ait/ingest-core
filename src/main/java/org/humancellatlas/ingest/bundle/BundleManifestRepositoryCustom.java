package org.humancellatlas.ingest.bundle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BundleManifestRepositoryCustom {
    Page<BundleManifest> findBundles(String projectUuid, String primarySubmissionUuid, Boolean isPrimary, Pageable pageable);
    Page<BundleManifest> findAllBundles(String projectUuid, String primarySubmissionUuid, Pageable pageable);
}
