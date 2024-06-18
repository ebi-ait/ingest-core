package org.humancellatlas.ingest.export;

import lombok.Data;
import lombok.NonNull;

@Data
public class ExportError {
  private final String errorCode;
  @NonNull private final String message;
  private final Object details;
}
