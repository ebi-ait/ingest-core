package org.humancellatlas.ingest.stagingjob.web;

import lombok.Data;

import java.util.UUID;

@Data
public class StagingJobsDeleteRequest {
    private UUID stagingAreaUuid;
}
