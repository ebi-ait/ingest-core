package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionEnvelopeService {

    @NonNull
    private final MessageRouter messageRouter;

    public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope,
            SubmissionState state) {
        if(! envelope.allowedStateTransitions().contains(state)) {
            throw new StateTransitionNotAllowed(String.format(
                    "Envelope with id %s cannot be transitioned from state %s to state %s",
                    envelope.getId(), envelope.getSubmissionState(), state));
        } else {
            messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);
        }
    }

}
