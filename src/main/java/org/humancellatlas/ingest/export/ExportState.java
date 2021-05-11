package org.humancellatlas.ingest.export;

public enum ExportState {
    QUEUED,
    EXPORTING,
    FAILED,
    EXPORTED,
    DEPRECATED
}
