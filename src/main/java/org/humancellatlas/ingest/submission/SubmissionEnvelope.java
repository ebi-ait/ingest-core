package org.humancellatlas.ingest.submission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.*;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class SubmissionEnvelope extends AbstractEntity {
    private final Map<String, ValidationState> validationStateMap = new HashMap<>();

    private @Setter StagingDetails stagingDetails;
    private SubmissionState submissionState;

    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

    public SubmissionEnvelope() {
        super(EntityType.SUBMISSION);
        this.submissionState = SubmissionState.PENDING;
        setUuid(new Uuid());
    }

    public SubmissionEnvelope(String id) {
        this();
        this.id = id;
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
                break;
            case PROCESSING:
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
