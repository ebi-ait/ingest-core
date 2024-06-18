package org.humancellatlas.ingest.export.job;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.ExportError;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.messaging.model.ExportSubmissionMessage;
import org.humancellatlas.ingest.messaging.model.SpreadsheetGenerationMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document
@CompoundIndexes({
  @CompoundIndex(name = "exportDestinationName", def = "{ 'destination.name': 1 }"),
  @CompoundIndex(name = "exportDestinationVersion", def = "{ 'destination.version': 1 }")
})
public class ExportJob implements Identifiable<String> {
  @Id @JsonIgnore private String id;

  @CreatedDate @Builder.Default private Instant createdDate = Instant.now();

  @Indexed
  @DBRef(lazy = true)
  @RestResource(exported = false)
  @JsonIgnore
  private final SubmissionEnvelope submission;

  private final ExportDestination destination;

  @Indexed @Builder.Default private ExportState status = ExportState.EXPORTING;

  @LastModifiedDate private Instant updatedDate;

  private Map<String, Object> context;

  @Builder.Default private List<ExportError> errors = new ArrayList<>();

  public ExportSubmissionMessage toExportSubmissionMessage(
      LinkGenerator linkGenerator, Map<String, Object> context) {
    String callbackLink = linkGenerator.createCallback(getClass(), getId());
    return new ExportSubmissionMessage(
        getId(),
        submission.getUuid().getUuid().toString(),
        destination.getContext().get("projectUuid").toString(),
        callbackLink,
        context);
  }

  public SpreadsheetGenerationMessage toGenerateSubmissionMessage(
      LinkGenerator linkGenerator, Map<String, Object> context) {
    // TODO: unify with toExportSubmissionMessage
    String callbackLink = linkGenerator.createCallback(getClass(), getId());
    return new SpreadsheetGenerationMessage(
        getId(),
        submission.getUuid().getUuid().toString(),
        destination.getContext().get("projectUuid").toString(),
        callbackLink,
        context);
  }
}
