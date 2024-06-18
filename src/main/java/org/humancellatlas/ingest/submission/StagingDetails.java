package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;

import lombok.Data;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/09/17
 */
@Data
class StagingDetails {
  private Uuid stagingAreaUuid;
  private StagingUrn stagingAreaLocation;
}
