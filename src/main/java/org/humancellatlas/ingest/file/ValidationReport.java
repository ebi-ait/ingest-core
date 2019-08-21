package org.humancellatlas.ingest.file;

import lombok.Data;

import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.state.ValidationState;

import java.util.List;
import java.util.UUID;

@Data
public class ValidationReport {
    private ValidationState validationState;
    private List<Object> validationErrors;
    
    protected ValidationReport() {}

}
