package org.humancellatlas.ingest.submission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.state.InvalidSubmissionStateException;
import org.humancellatlas.ingest.state.MetadataDocumentStateException;
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
    private final List<Event> events = new ArrayList<>();
    private final Map<String, ValidationState> validationStateMap = new HashMap<>();

    private @Setter StagingDetails stagingDetails;
    private SubmissionState submissionState;

    private @DBRef List<BundleManifest> bundleManifests = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(SubmissionEnvelope.class);

    private static Logger getLog() {
        return log;
    }

    public SubmissionEnvelope() {
        super(EntityType.SUBMISSION);
        this.submissionState = SubmissionState.PENDING;
        setUuid(new Uuid());
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
                getLog().warn(String.format("There are no legal state transitions for '%s' state", fromState.name()));
                break;
        }
        return allowedStates;
    }

    public List<SubmissionState> allowedStateTransitions() {
        return allowedStateTransitions(getSubmissionState());
    }

    public SubmissionEnvelope addEvent(Event event) {
        this.events.add(event);

        return this;
    }

    public SubmissionEnvelope addCreatedBundleManifest(BundleManifest bundleManifest) {
        this.bundleManifests.add(bundleManifest);

        return this;
    }

    public SubmissionEnvelope enactStateTransition(SubmissionState targetState) {
        if (!allowedStateTransitions().contains(targetState)) {
            throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                                                                            "as a submission envelope state that can be set",
                                                                    submissionState.name()));
        }

        if (this.submissionState != targetState) {
            this.submissionState = targetState;
        }

        return this;
    }

    public boolean flagPossibleMetadataDocumentStateChange(MetadataDocument metadataDocument) {
        if (!isTrackingMetadata(metadataDocument)) {
            // if this doc is in draft, it's either a new document or has new content, so it's ok to add to state tracker
            // but if not, we need to throw an exception here
            if (!metadataDocument.getValidationState().equals(ValidationState.DRAFT)) {
                throw new MetadataDocumentStateException(String.format(
                        "Metadata document '%s: %s', in state '%s', was not being tracked by containing envelope %s",
                        metadataDocument.getClass().getSimpleName(),
                        metadataDocument.getId(),
                        metadataDocument.getValidationState(),
                        this.getId(), metadataDocument.getOpenSubmissionEnvelope().getId()));
            }
            else {
                doValidationStateUpdate(metadataDocument);
                return true;
            }
        }
        else {
            // already tracking, update if this is a change
            if (!this.validationStateMap.get(metadataDocument.getId()).equals(metadataDocument.getValidationState())) {
                doValidationStateUpdate(metadataDocument);
                return true;
            }
        }

        return false;
    }

    private void doValidationStateUpdate(MetadataDocument metadataDocument) {
        this.validationStateMap.put(metadataDocument.getId(), metadataDocument.getValidationState());
    }

    public SubmissionState determineEnvelopeState() {
        final Iterator<Map.Entry<String, ValidationState>> validationStateMapIterator =
                validationStateMap.entrySet().iterator();

        boolean isSomethingValidating = false;
        boolean isSomethingInvalid = false;

        while (validationStateMapIterator.hasNext()) {
            Map.Entry<String, ValidationState> entry = validationStateMapIterator.next();
            ValidationState nextTrackedDocumentState = entry.getValue();

            switch (nextTrackedDocumentState) {
                case VALIDATING:
                    // this document is validating, so we can set the envelope state to validating (as long as nothing is invalid)
                    isSomethingValidating = true;
                    break;
                case VALID:
                    // this document has finished validating, we can remove it from the map
                    validationStateMapIterator.remove();
                    break;
                case INVALID:
                    // if the envelope is already invalid, we can continue cleaning up...
                    isSomethingInvalid = true;
                    // but if the envelope is not invalid, we need to flag the state change
                    if (!getSubmissionState().equals(SubmissionState.INVALID)) {
                        // need to mark as invalid immediately
                        return SubmissionState.INVALID;
                    }
            }
        }

        // decision time!
        // according to spec...
        //     - if >=1 metadata documents are invalid, the envelope is invalid
        //     - otherwise, if >=1 metadata documents are validating, then the envelope is validating
        //     - if everything is now valid (state > DRAFT and nothing in the validationStateMap) the envelope is valid
        //     - if there are documents in the queue but state is PENDING, upgrade to DRAFT
        //     - otherwise, nothing changes

        if (isSomethingInvalid) {
            return SubmissionState.INVALID;
        }
        if (isSomethingValidating) {
            return SubmissionState.VALIDATING;
        }
        if (hasReceivedDocuments() && !isTrackingMetadata()) {
            return SubmissionState.VALID;
        }
        if (isTrackingMetadata()) {
            // PENDING -> DRAFT or there are non-invalid docs still being tracked
            return SubmissionState.DRAFT;
        }

        return getSubmissionState();
    }

    public boolean hasReceivedDocuments() {
        return getSubmissionState().compareTo(SubmissionState.PENDING) > 0;
    }

    public @JsonIgnore boolean isTrackingMetadata() {
        return validationStateMap.size() > 0;
    }

    public @JsonIgnore boolean isTrackingMetadata(MetadataDocument metadataDocument) {
        return validationStateMap.containsKey(metadataDocument.getId());
    }

    public boolean isOpen() {
        List<SubmissionState> states = Arrays.asList(SubmissionState.values());
        return states.indexOf(this.getSubmissionState()) < states.indexOf(SubmissionState.SUBMITTED);
    }
}
