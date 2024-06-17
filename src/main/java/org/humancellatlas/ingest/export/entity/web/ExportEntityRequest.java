package org.humancellatlas.ingest.export.entity.web;

import java.util.List;
import java.util.Map;

import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;

import lombok.Data;
import lombok.NonNull;

@Data
public class ExportEntityRequest {
  @NonNull ExportState status;

  @NonNull Map<String, Object> context;

  @NonNull List<ExportError> errors;
}
