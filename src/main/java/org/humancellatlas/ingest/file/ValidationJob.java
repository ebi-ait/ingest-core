package org.humancellatlas.ingest.file;

import lombok.Data;
import org.humancellatlas.ingest.core.Checksums;

import java.util.UUID;

@Data
public class ValidationJob {
    private UUID validationId;
    private Checksums checksums;

    protected ValidationJob() {}

}
