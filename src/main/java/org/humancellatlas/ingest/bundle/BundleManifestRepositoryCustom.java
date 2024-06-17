package org.humancellatlas.ingest.bundle;

import org.humancellatlas.ingest.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BundleManifestRepositoryCustom {
  Page<BundleManifest> findBundleManifestsByProjectAndBundleType(
      Project project, BundleType bundleType, Pageable pageable);
}
