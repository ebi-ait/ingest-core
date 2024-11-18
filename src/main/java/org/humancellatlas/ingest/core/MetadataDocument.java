package org.humancellatlas.ingest.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
@EqualsAndHashCode(callSuper = true, exclude = "contentLastModified")
public abstract class MetadataDocument extends AbstractEntity {

  @Setter private Instant firstDcpVersion;

  private Instant dcpVersion;

  private Instant contentLastModified;

  private Object content;

  // This property holds the reference to the submissionEnvelope this metadata document was part of.
  // A metadata document is part of one submissionEnvelope.
  // The other end of this relationship can be defined as a set of metadataDocuments in a
  // submissionEnvelope.
  @Indexed
  @Setter
  @DBRef(lazy = true)
  private SubmissionEnvelope submissionEnvelope;

  @Field("accessions")
  private List<String> accessions;

//  private @Setter Accession accession;

  private @Setter ValidationState validationState;

  private @Setter List<Object> validationErrors;

  private @Setter List<String> graphValidationErrors;

  private @Setter @Field Boolean isUpdate = false;

  private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

  protected static Logger getLog() {
    return log;
  }

  protected MetadataDocument() {}

  protected MetadataDocument(EntityType type, Object content) {
    super(type);
    this.content = content;
    this.contentLastModified = Instant.now();
    this.validationState = ValidationState.DRAFT;
  }

  public static List<ValidationState> allowedStateTransitions(ValidationState fromState) {
    List<ValidationState> allowedStates = new ArrayList<>();
    switch (fromState) {
      case DRAFT:
        allowedStates.add(ValidationState.VALIDATING);
        break;
      case VALIDATING:
        allowedStates.add(ValidationState.VALID);
        allowedStates.add(ValidationState.INVALID);
        break;
      case VALID:
        allowedStates.add(ValidationState.PROCESSING);
        allowedStates.add(ValidationState.DRAFT);
        break;
      case INVALID:
        allowedStates.add(ValidationState.DRAFT);
        break;
      case PROCESSING:
        allowedStates.add(ValidationState.COMPLETE);
        allowedStates.add(ValidationState.DRAFT);
        break;
      default:
        getLog()
            .warn(
                String.format(
                    "There are no legal state transitions for '%s' state", fromState.name()));
        break;
    }
    return allowedStates;
  }

  public List<ValidationState> allowedStateTransitions() {
    return allowedStateTransitions(getValidationState());
  }

  public MetadataDocument enactStateTransition(ValidationState targetState) {
    this.validationState = targetState;
    return this;
  }

  public void setContent(Object content) {
    if (this.content == null || !this.content.equals(content)) {
      this.content = content;
      this.contentLastModified = Instant.now();
      this.setDcpVersion(this.contentLastModified);
    }
  }

  public MetadataDocument setDcpVersion(Instant dcpVersion) {
    // DCP version should never be set to null
    if (dcpVersion != null) {
      if (this.dcpVersion == null) {
        this.firstDcpVersion = dcpVersion;
      }
      this.dcpVersion = dcpVersion;
    }
    return this;
  }

  public List<String> getAccessions() {
    return accessions;
  }

  public void setAccessions(List<String> accessions) {
    this.accessions = accessions;
  }
}
