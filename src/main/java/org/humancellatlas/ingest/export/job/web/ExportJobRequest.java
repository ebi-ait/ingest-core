package org.humancellatlas.ingest.export.job.web;

import java.util.Map;

import org.humancellatlas.ingest.export.destination.ExportDestination;

import lombok.Data;
import lombok.NonNull;

@Data
public class ExportJobRequest {
  @NonNull ExportDestination destination;

  @NonNull Map<String, Object> context;
}
