package org.humancellatlas.ingest.export.job.web;

import lombok.Data;
import lombok.NonNull;
import org.humancellatlas.ingest.export.destination.ExportDestination;

import java.util.Map;

@Data
public class ExportJobRequest {
    @NonNull
    ExportDestination destination;

    @NonNull
    Map<String, Object> context;
}
