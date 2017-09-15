package org.humancellatlas.ingest.submission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/09/17
 */
@RequiredArgsConstructor
@Getter
class StagingDetails {
    private final Uuid stagingAreaUuid;
    private final StagingUrn stagingAreaLocation;
}
