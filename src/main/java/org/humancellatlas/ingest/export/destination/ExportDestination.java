package org.humancellatlas.ingest.export.destination;

import lombok.Data;
import lombok.NonNull;

@Data
public class ExportDestination {
    private final ExportDestinationName name;
    private final String version;
    private final Object context;
}

