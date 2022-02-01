package org.humancellatlas.ingest.submission;

import lombok.Data;

import java.time.Instant;

@Data
public class SpreadsheetGenerationJob {
    private Instant finishedDate;
    private Instant createdDate;
}
