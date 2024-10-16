package uk.ac.ebi.subs.ingest.bundle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import uk.ac.ebi.subs.ingest.project.Project;

public interface BundleManifestRepositoryCustom {
  Page<BundleManifest> findBundleManifestsByProjectAndBundleType(
      Project project, BundleType bundleType, Pageable pageable);
}
