package org.humancellatlas.ingest.export.destination;

import lombok.Data;
import lombok.NonNull;

@Data
public class ExportDestination {
    @NonNull
    private final ExportDestinationName name;
    @NonNull
    private final String version;
    private final Object context;
}

