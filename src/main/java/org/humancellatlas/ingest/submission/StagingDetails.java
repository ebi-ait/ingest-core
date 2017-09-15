package org.humancellatlas.ingest.submission;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;

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
