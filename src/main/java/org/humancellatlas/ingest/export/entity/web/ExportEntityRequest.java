package org.humancellatlas.ingest.export.entity.web;

import lombok.Data;
import lombok.NonNull;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;

import java.util.List;

@Data
public class ExportEntityRequest {
    @NonNull
    ExportState status;

    @NonNull
    Object context;

    @NonNull
    List<ExportError> errors;
}
