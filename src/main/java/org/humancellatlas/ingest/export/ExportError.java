package org.humancellatlas.ingest.export;

import lombok.Data;

@Data
public class ExportError {
    private final String errorCode;
    private final String message;
    private final Object details;
}
