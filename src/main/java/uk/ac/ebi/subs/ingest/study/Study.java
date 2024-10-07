package uk.ac.ebi.subs.ingest.study;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.ac.ebi.subs.ingest.core.DescriptiveSchema;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Getter
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"submissionEnvelopes"})
@JsonIgnoreProperties({"firstDcpVersion", "dcpVersion"})
public class Study extends MetadataDocument implements DescriptiveSchema {
  // A study may have 1 or more submissions related to it.
  @JsonIgnore
  private @DBRef(lazy = true) Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

  // A study can have multiple datasets
  @RestResource private Set<Dataset> datasets = new HashSet<>();

  @Field("described_by")
  private String describedBy;

  @Field("schema_version")
  private String schemaVersion;

  @Field("schema_type")
  private String schemaType;

  @JsonCreator
  public Study(
      @JsonProperty("described_by") String describedBy,
      @JsonProperty("schema_version") String schemaVersion,
      @JsonProperty("schema_type") String schemaType,
      @JsonProperty("content") Object content) {
    super(EntityType.STUDY, content);
    this.describedBy = describedBy;
    this.schemaVersion = schemaVersion;
    this.schemaType = schemaType;
  }

  public void addToSubmissionEnvelopes(@NotNull SubmissionEnvelope submissionEnvelope) {
    this.submissionEnvelopes.add(submissionEnvelope);
  }

  // Override MorphicDescriptiveSchema methods
  @Override
  public String getDescribedBy() {
    return describedBy;
  }

  @Override
  public void setDescribedBy(String describedBy) {
    this.describedBy = describedBy;
  }

  @Override
  public String getSchemaVersion() {
    return schemaVersion;
  }

  @Override
  public void setSchemaVersion(String schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  @Override
  public String getSchemaType() {
    return schemaType;
  }

  @Override
  public void setSchemaType(String schemaType) {
    this.schemaType = schemaType;
  }

  public void addDataset(final Dataset dataset) {
    datasets.add(dataset);
  }

  // ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
  @JsonIgnore
  public List<SubmissionEnvelope> getOpenSubmissionEnvelopes() {
    return this.submissionEnvelopes.stream()
        .filter(Objects::nonNull)
        .filter(env -> env.getSubmissionState() != null)
        .filter(SubmissionEnvelope::isOpen)
        .collect(Collectors.toList());
  }

  public Boolean getHasOpenSubmission() {
    return !getOpenSubmissionEnvelopes().isEmpty();
  }

  @JsonIgnore
  public Boolean isEditable() {
    return this.submissionEnvelopes.stream()
        .filter(Objects::nonNull)
        .allMatch(SubmissionEnvelope::isEditable);
  }
}