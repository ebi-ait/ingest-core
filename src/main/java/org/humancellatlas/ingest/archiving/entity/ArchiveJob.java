package org.humancellatlas.ingest.archiving.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArchiveJob {

    protected @Id String id;
    private String submissionUuid;
    private Instant createdDate;
    private Instant responseDate;
    private ArchiveJobStatus overallStatus;
    private Map<?, ?> resultsFromArchives;

    public enum ArchiveJobStatus {
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
