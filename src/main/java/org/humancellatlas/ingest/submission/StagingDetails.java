package org.humancellatlas.ingest.submission;

import lombok.Data;
import org.humancellatlas.ingest.core.Uuid;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett

 */
@Data
class StagingDetails {
    private Uuid stagingAreaUuid;
    private StagingUrn stagingAreaLocation;
}
