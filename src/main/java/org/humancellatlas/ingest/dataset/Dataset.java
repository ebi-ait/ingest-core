package org.humancellatlas.ingest.dataset;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties({
  "firstDcpVersion",
  "dcpVersion",
  "validationState",
  "validationErrors",
  "graphValidationErrors",
  "isUpdate"
})
public class Dataset extends MetadataDocument {
  // non-final always
  @JsonIgnore
  @DBRef(lazy = true)
  private Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();

  private Set<String> dataFiles = new HashSet<>();

  private Set<String> biomaterials = new HashSet<>();

  private Set<Protocol> protocols = new HashSet<>();

  private Set<String> processes = new HashSet<>();

  @Setter private String comment;

  @JsonCreator
  public Dataset(@JsonProperty("content") final Object content) {
    super(EntityType.DATASET, content);
  }

  public void addToSubmissionEnvelopes(@NotNull final SubmissionEnvelope submissionEnvelope) {
    this.submissionEnvelopes.add(submissionEnvelope);
  }

  public void addBiomaterial(@NotNull final String biomaterialId) {
    this.biomaterials.add(biomaterialId);
  }

  public void addProtocol(@NotNull final Protocol protocol) {
    this.protocols.add(protocol);
  }

  public void addProcess(@NotNull final String processId) {
    this.processes.add(processId);
  }

  public void addFile(@NotNull final String dataFileId) {
    this.dataFiles.add(dataFileId);
  }

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
