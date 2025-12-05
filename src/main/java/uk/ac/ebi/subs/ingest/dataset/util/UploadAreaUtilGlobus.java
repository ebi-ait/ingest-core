package uk.ac.ebi.subs.ingest.dataset.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.subs.ingest.dataset.Dataset;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadAreaUtilGlobus {

  private final GlobusService globus;
  private final GlobusProps props;

  @Value("${morphic.dataset.subdirs:incoming,metadata,checksums}")
  private String datasetSubdirs;

  public void createDataFilesUploadArea(final Dataset dataset, final String principalId) {
    final String datasetId = dataset.getId();
    log.info(
        "[UploadAreaUtilGlobus] createDataFilesUploadArea datasetId={} principal={}",
        datasetId,
        principalId);

    String logicalPath;
    try {
      logicalPath = globus.remoteDatasetRoot(datasetId);
    } catch (IllegalStateException e) {
      log.warn("[Globus] base-path is not configured; cannot prepare upload area");
      dataset.setComment("Globus upload area is not configured (missing base-path).");
      return;
    }

    log.info(
        "[Globus] Preparing upload area on collection={} at {} for datasetId={}",
        props.getCollectionId(),
        logicalPath,
        datasetId);

    try {
      globus.mkdir(logicalPath);

      if (props.isEnableAcl()) {
        if (principalId == null || principalId.isBlank()) {
          log.warn(
              "[Globus] No principalId provided; skipping ACL creation for datasetId={}",
              datasetId);
        } else {
          String aclPath = "/" + datasetId + "/";

          globus.addAclRule(aclPath, principalId, "rw");
          log.info(
              "[Globus] Applied ACL for datasetId={} path={} principal={} perms=rw",
              datasetId,
              aclPath,
              principalId);
        }
      } else {
        log.info(
            "[Globus] ACL creation disabled via config; skipping ACL rule for datasetId={}",
            datasetId);
      }

      dataset.setComment(
          "Upload area ready on Globus collection "
              + props.getCollectionId()
              + " at "
              + logicalPath
              + ".");

    } catch (Exception e) {
      log.warn(
          "[Globus] Failed to prepare upload area for datasetId={} at {} : {}",
          datasetId,
          logicalPath,
          e.getMessage(),
          e);
      dataset.setComment(
          "Upload area preparation on Globus failed at " + logicalPath + " (see server logs).");
    }
  }

  public void deleteDataFilesAndUploadArea(final String datasetId) {
    log.info(
        "[Globus] deleteDataFilesAndUploadArea called for {} (not implemented yet)", datasetId);
  }
}
