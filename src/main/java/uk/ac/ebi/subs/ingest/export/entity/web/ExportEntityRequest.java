package uk.ac.ebi.subs.ingest.export.entity.web;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.export.ExportError;
import uk.ac.ebi.subs.ingest.export.ExportState;

@Data
public class ExportEntityRequest {
  @NonNull ExportState status;

  @NonNull Map<String, Object> context;

  @NonNull List<ExportError> errors;
}
