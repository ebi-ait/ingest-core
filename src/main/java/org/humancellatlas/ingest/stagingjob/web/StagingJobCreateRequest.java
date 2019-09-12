package org.humancellatlas.ingest.stagingjob.web;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class StagingJobCreateRequest {
    private final @NonNull UUID stagingAreaUuid;
    private final @NonNull String stagingAreaFileName;
    private final @NonNull UUID metadataUuid;
}
