package org.humancellatlas.ingest.export.destination;

import java.util.Map;

import lombok.Data;

@Data
public class ExportDestination {
  private final ExportDestinationName name;
  private final String version;
  private final Map<String, Object> context;
}
