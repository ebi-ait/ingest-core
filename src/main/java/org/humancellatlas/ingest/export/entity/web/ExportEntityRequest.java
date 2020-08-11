package org.humancellatlas.ingest.export.entity.web;

import lombok.Data;
import lombok.NonNull;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;

import java.util.List;
import java.util.Map;

@Data
public class ExportEntityRequest {
    @NonNull
    ExportState status;

    @NonNull
    Map<String, Object> context;

    @NonNull
    List<ExportError> errors;
}
