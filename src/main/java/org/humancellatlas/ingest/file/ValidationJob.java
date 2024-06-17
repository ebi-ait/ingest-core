package org.humancellatlas.ingest.file;

import java.util.UUID;

import org.humancellatlas.ingest.core.Checksums;

import lombok.Data;

@Data
public class ValidationJob {
  private UUID validationId;
  private Checksums checksums;
  private boolean jobCompleted;
  private ValidationReport validationReport;

  protected ValidationJob() {}
}
