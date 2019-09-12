package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.errors.SubmissionError;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @NonNull
    private final SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

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
        messageRouter.routeSubmissionRequiresProcessingMessage(envelope);
    }

    public void processSubmission(SubmissionEnvelope submissionEnvelope) {
        if(submissionEnvelope.getIsUpdate()) {
            processUpdateSubmission(submissionEnvelope);
        } else {
            processOriginalSubmission(submissionEnvelope);
        }
    }

    private void processOriginalSubmission(SubmissionEnvelope submissionEnvelope) {
        try {
            exporter.exportBundles(submissionEnvelope);
        }
        catch (Exception e) {
            log.error("Uncaught Exception exporting Bundles", e);
        }
    }

    private void processUpdateSubmission(SubmissionEnvelope submissionEnvelope) {
        try {
            metadataUpdateService.applyUpdates(submissionEnvelope);
            exporter.updateBundles(submissionEnvelope);
        }
        catch (Exception e) {
            log.error("Uncaught Exception Applying Updates or Exporting Bundles", e);
        }
    }

    public SubmissionEnvelope createUpdateSubmissionEnvelope() {
        SubmissionEnvelope updateSubmissionEnvelope = new SubmissionEnvelope();
        submissionEnvelopeCreateHandler.setUuid(updateSubmissionEnvelope);
        updateSubmissionEnvelope.setIsUpdate(true);
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = submissionEnvelopeRepository.insert(updateSubmissionEnvelope);
        submissionEnvelopeCreateHandler.handleSubmissionEnvelopeCreation(updateSubmissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }

    public Optional<SubmissionEnvelope> getSubmissionById(String submissionId) {
        return submissionEnvelopeRepository.findById(submissionId);
    }
}
