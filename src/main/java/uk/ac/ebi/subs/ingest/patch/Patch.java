package uk.ac.ebi.subs.ingest.patch;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ebi.subs.ingest.core.AbstractEntity;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Data
@AllArgsConstructor
@Document
@EqualsAndHashCode(callSuper = true)
public class Patch<T extends MetadataDocument> extends AbstractEntity {
  private Map<String, Object> jsonPatch;
  private @DBRef SubmissionEnvelope submissionEnvelope;

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
  private @DBRef T originalDocument;

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
  private @DBRef T updateDocument;
}
