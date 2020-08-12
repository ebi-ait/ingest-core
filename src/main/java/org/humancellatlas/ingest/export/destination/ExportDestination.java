package org.humancellatlas.ingest.export.destination;

import lombok.Data;

import java.util.Map;

@Data
public class ExportDestination {
    private final ExportDestinationName name;
    private final String version;
    private final Map<String, Object> context;
}

