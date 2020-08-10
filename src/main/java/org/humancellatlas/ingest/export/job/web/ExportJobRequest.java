package org.humancellatlas.ingest.export.job.web;

import lombok.Data;
import org.humancellatlas.ingest.export.destination.ExportDestination;

@Data
public class ExportJobRequest {
    Object context;
    ExportDestination destination;
}
