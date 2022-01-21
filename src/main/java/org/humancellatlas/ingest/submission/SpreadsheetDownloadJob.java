package org.humancellatlas.ingest.submission;

import lombok.Data;

import java.time.Instant;

@Data
public class SpreadsheetDownloadJob {
    private Instant finishedDate;
    private Instant createdDate;
}
