package uk.ac.ebi.subs.ingest.export.job.web;

import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestination;

@Data
public class ExportJobRequest {
  @NonNull ExportDestination destination;

  @NonNull Map<String, Object> context;
}
