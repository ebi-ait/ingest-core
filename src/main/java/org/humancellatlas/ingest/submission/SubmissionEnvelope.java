package org.humancellatlas.ingest.submission;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.SubmitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

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
    private @Setter
    StagingDetails stagingDetails;
    private SubmissionState submissionState;
    private @Setter
    Boolean triggersAnalysis;
    private @Setter
    Boolean isUpdate;
    private @Setter
    Set<SubmitAction> submitActions;

    public SubmissionEnvelope() {
        super(EntityType.SUBMISSION);
        this.submissionState = SubmissionState.PENDING;
        this.triggersAnalysis = true;
        this.isUpdate = false;
        this.submitActions = new HashSet<>();
    }

    public SubmissionEnvelope(String id) {
        this();
        this.id = id;
    }

    private static Logger getLog() {
        return log;
    }

    public static List<SubmissionState> allowedStateTransitions(SubmissionState fromState) {
        List<SubmissionState> allowedStates = new ArrayList<>();
        switch (fromState) {
            case PENDING:
                allowedStates.add(SubmissionState.DRAFT);
                break;
            case DRAFT:
                allowedStates.add(SubmissionState.VALIDATING);
                break;
            case VALIDATING:
                allowedStates.add(SubmissionState.DRAFT);
                allowedStates.add(SubmissionState.VALID);
                allowedStates.add(SubmissionState.INVALID);
                break;
            case VALID:
                allowedStates.add(SubmissionState.DRAFT);
                allowedStates.add(SubmissionState.SUBMITTED);
                break;
            case INVALID:
                allowedStates.add(SubmissionState.DRAFT);
                allowedStates.add(SubmissionState.VALIDATING);
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

    public List<SubmissionState> allowedStateTransitions() {
        return allowedStateTransitions(getSubmissionState());
    }


    public SubmissionEnvelope enactStateTransition(SubmissionState targetState) {
        if (this.submissionState != targetState) {
            this.submissionState = targetState;
        }

        return this;
    }

    public boolean isOpen() {
        List<SubmissionState> states = Arrays.asList(SubmissionState.values());
        return states.indexOf(this.getSubmissionState()) < states.indexOf(SubmissionState.SUBMITTED);
    }
}
