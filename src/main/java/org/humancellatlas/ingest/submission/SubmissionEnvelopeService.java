package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SubmissionEnvelopeService {

    @NonNull
    private final MessageRouter messageRouter;

    @NonNull
    private final Exporter exporter;

    @NonNull
    private final MetadataUpdateService metadataUpdateService;

    @NonNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @NonNull
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @NonNull
    private final SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

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

    public void handleSubmissionRequest(SubmissionEnvelope envelope) {
        if(! envelope.getIsUpdate()) {
            handleSubmitOriginalSubmission(envelope);
        } else {
            handleSubmitUpdateSubmission(envelope);
        }
    }

    private void handleSubmitOriginalSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> exporter.exportBundles(submissionEnvelope));
    }

    private void handleSubmitUpdateSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            metadataUpdateService.upsertUpdates(submissionEnvelope);
            // TODO: exportUpdateBundles()
        });
    }


    public SubmissionEnvelope addErrorToEnvelope(SubmissionError submissionError, SubmissionEnvelope submissionEnvelope) {
        submissionEnvelope.addError(submissionError);
        return submissionEnvelopeRepository.save(submissionEnvelope);
    }

    public SubmissionEnvelope createUpdateSubmissionEnvelope() {
        SubmissionEnvelope updateSubmissionEnvelope = new SubmissionEnvelope();
        submissionEnvelopeCreateHandler.setUuid(updateSubmissionEnvelope);
        updateSubmissionEnvelope.setIsUpdate(true);
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = submissionEnvelopeRepository.insert(updateSubmissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }
}
