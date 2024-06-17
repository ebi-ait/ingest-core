package org.humancellatlas.ingest.stagingjob.web;

import java.util.UUID;

import lombok.Data;

@Data
public class StagingJobCreateRequest {
  private UUID stagingAreaUuid;
  private String stagingAreaFileName;
}
