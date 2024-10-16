package uk.ac.ebi.subs.ingest.submission;

import java.time.Instant;

import lombok.Data;

@Data
public class SpreadsheetGenerationJob {
  private Instant finishedDate;
  private Instant createdDate;
}
