package org.humancellatlas.ingest.submission;

import java.time.Instant;

import lombok.Data;

@Data
public class SpreadsheetGenerationJob {
  private Instant finishedDate;
  private Instant createdDate;
}
