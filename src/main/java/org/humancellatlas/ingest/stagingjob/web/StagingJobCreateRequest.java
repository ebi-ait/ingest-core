package org.humancellatlas.ingest.stagingjob.web;

import lombok.Data;

import java.util.UUID;

@Data
public class StagingJobCreateRequest {
    private UUID stagingAreaUuid;
    private String stagingAreaFileName;
}
