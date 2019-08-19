package org.humancellatlas.ingest.file;

import lombok.Data;

import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.state.ValidationState;

import java.util.List;
import java.util.UUID;

@Data
public class ValidationJob {
    private UUID validationId;
    private Checksums checksums;
    private boolean jobCompleted;
    private ValidationState validationState;
    private List<Object> validationErrors;
    
    // TODO: maybe create new ValidationReport object with validationState, validationErrors: List<String>?

    protected ValidationJob() {}

}
