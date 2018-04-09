package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.core.service.AssayService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by rolando on 10/03/2018.
 */
@Service
@RequiredArgsConstructor
public class SubmissionEnvelopeService {
    @Autowired @NonNull private final MessageRouter messageRouter;
    @Autowired @NonNull private final AssayService assayService;


    public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope, SubmissionState state) {
        if(! envelope.allowedStateTransitions().contains(state)) {
            throw new StateTransitionNotAllowed(String.format("Envelope with id %s cannot be transitioned from state %s to state %s",
                                                              envelope.getId(), envelope.getSubmissionState(), state));
        } else {
            messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);
        }
    }

    //TODO deprecate or delete this
    public void handleSubmit(SubmissionEnvelope envelope) {
        assayService.identifyAssaysFor(envelope);
    }

}
