package uk.ac.ebi.subs.ingest.submission;

import lombok.Data;
import uk.ac.ebi.subs.ingest.core.Uuid;

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
