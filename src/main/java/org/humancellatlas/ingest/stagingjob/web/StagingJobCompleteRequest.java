package org.humancellatlas.ingest.stagingjob.web;

import lombok.Data;

@Data
public class StagingJobCompleteRequest {
  private String stagingAreaFileUri;
}
