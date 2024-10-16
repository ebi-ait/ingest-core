package uk.ac.ebi.subs.ingest.stagingjob.web;

import lombok.Data;

@Data
public class StagingJobCompleteRequest {
  private String stagingAreaFileUri;
}
