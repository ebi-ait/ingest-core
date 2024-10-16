package uk.ac.ebi.subs.ingest.export.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.subs.ingest.export.ExportError;
import uk.ac.ebi.subs.ingest.export.ExportState;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;

@Data
@Builder
@Document
public class ExportEntity implements Identifiable<String> {
  @Id @JsonIgnore private String id;

  @Indexed
  @DBRef(lazy = true)
  @RestResource(exported = false)
  @JsonIgnore
  private ExportJob exportJob;

  @Indexed private ExportState status;

  @CreatedDate private Instant createdDate;

  private Map<String, Object> context;

  @Builder.Default private List<ExportError> errors = new ArrayList<>();
}
