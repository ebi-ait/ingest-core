package uk.ac.ebi.subs.ingest.archiving.entity;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

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
