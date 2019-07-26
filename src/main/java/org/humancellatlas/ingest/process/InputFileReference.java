package org.humancellatlas.ingest.process;

import lombok.Data;

import java.util.UUID;

@Data
public class InputFileReference {
    private UUID inputFileUuid;
}
