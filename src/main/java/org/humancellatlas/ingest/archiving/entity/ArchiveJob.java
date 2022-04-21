package org.humancellatlas.ingest.archiving.entity;

import lombok.Data;
import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class ArchiveJob {

    private Uuid uuid;
    private String submissionUuid;
    private Instant createdDate;
    private Instant responseDate;
    private ArchiveJobStatus overallStatus;
    private Object resultsFromArchives;

    enum ArchiveJobStatus {
        PENDING("Pending"),
        RUNNING("Running"),
        FAILED("Failed"),
        COMPLETED("Completed");

        final String status;

        ArchiveJobStatus(String status) {
            this.status = status;
        }
    }
}
