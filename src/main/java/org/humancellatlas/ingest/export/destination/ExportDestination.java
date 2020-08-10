package org.humancellatlas.ingest.export.destination;

import lombok.Data;

@Data
public class ExportDestination {
    private final ExportDestinationName destinationName;
    private final String version;
    private final Object context;
}

