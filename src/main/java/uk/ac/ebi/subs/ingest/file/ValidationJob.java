package uk.ac.ebi.subs.ingest.file;

import java.util.UUID;

import lombok.Data;
import uk.ac.ebi.subs.ingest.core.Checksums;

@Data
public class ValidationJob {
  private UUID validationId;
  private Checksums checksums;
  private boolean jobCompleted;
  private ValidationReport validationReport;

  protected ValidationJob() {}
}
