package uk.ac.ebi.subs.ingest.submission;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.core.AbstractEntity;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.state.SubmitAction;

@Getter
@Document
/*
Used as a workaround to inheritance issue.
Not proper to annotate uuid in parent class as we don't want uuid index for all subtypes.
*/
@CompoundIndex(def = "{ 'uuid': 1 }", unique = true)
@EqualsAndHashCode(callSuper = true)
public class SubmissionEnvelope extends AbstractEntity {
  private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);
  private @Setter StagingDetails stagingDetails;
  private @Setter Boolean triggersAnalysis;
  private @Setter Boolean isUpdate;
  private @Setter Set<SubmitAction> submitActions;
  private @Setter SpreadsheetGenerationJob lastSpreadsheetGenerationJob;
  private SubmissionState submissionState;

  public SubmissionEnvelope() {
    super(EntityType.SUBMISSION);
    this.submissionState = SubmissionState.PENDING;
    this.triggersAnalysis = true;
    this.isUpdate = false;
    this.submitActions = new HashSet<>();
  }

  public static List<SubmissionState> allowedSubmissionStateTransitions(SubmissionState fromState) {
    List<SubmissionState> allowedStates = new ArrayList<>();

    switch (fromState) {
      case PENDING:
      case METADATA_INVALID:
      case GRAPH_INVALID:
        allowedStates.add(SubmissionState.DRAFT);
        break;
      case DRAFT:
        allowedStates.add(SubmissionState.METADATA_VALID);
        allowedStates.add(SubmissionState.METADATA_INVALID);
        break;
      case METADATA_VALID:
        allowedStates.add(SubmissionState.DRAFT);
        allowedStates.add(SubmissionState.METADATA_INVALID);
        allowedStates.add(SubmissionState.GRAPH_VALID);
        allowedStates.add(SubmissionState.GRAPH_INVALID);
        break;
      case GRAPH_VALID:
        allowedStates.add(SubmissionState.SUBMITTED);
        allowedStates.add(SubmissionState.DRAFT);
        break;
      case SUBMITTED:
        allowedStates.add(SubmissionState.PROCESSING);
        allowedStates.add(SubmissionState.EXPORTING);
        break;
      case PROCESSING:
        allowedStates.add(SubmissionState.ARCHIVING);
        break;
      case ARCHIVING:
        allowedStates.add(SubmissionState.ARCHIVED);
        break;
      case ARCHIVED:
        allowedStates.add(SubmissionState.EXPORTING);
        break;
      case EXPORTED:
        allowedStates.add(SubmissionState.CLEANUP);
        break;
      case CLEANUP:
        allowedStates.add(SubmissionState.COMPLETE);
        break;
      default:
        break;
    }

    return allowedStates;
  }

  public List<SubmissionState> allowedSubmissionStateTransitions() {
    return allowedSubmissionStateTransitions(getSubmissionState());
  }

  public void enactStateTransition(SubmissionState targetState) {
    if (this.submissionState != targetState) {
      this.submissionState = targetState;
    }
  }

  public boolean isOpen() {
    List<SubmissionState> states = Arrays.asList(SubmissionState.values());
    return states.indexOf(this.getSubmissionState()) < states.indexOf(SubmissionState.SUBMITTED);
  }

  private List<SubmissionState> getNonEditableStates() {
    return Arrays.asList(
        /*SubmissionState.PENDING,*/
        SubmissionState.EXPORTING,
        SubmissionState.PROCESSING,
        SubmissionState.CLEANUP,
        SubmissionState.ARCHIVED,
        SubmissionState.SUBMITTED);
  }

  public boolean isEditable() {
    return !this.getNonEditableStates().contains(this.submissionState);
  }

  public boolean isSystemEditable() {
    return this.getNonEditableStates().stream()
        .filter(state -> state != SubmissionState.CLEANUP)
        .filter(state -> state != SubmissionState.PENDING)
        .noneMatch(state -> state == this.submissionState);
  }
}
