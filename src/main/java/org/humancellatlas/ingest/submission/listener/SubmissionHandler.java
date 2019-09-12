package org.humancellatlas.ingest.submission.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SubmissionHandler {
    private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CompletableFuture<String> handleProcessSubmission(String submissionId) {
        return CompletableFuture.supplyAsync(() -> submissionEnvelopeService.getSubmissionById(submissionId))
                                .thenApply(maybeSubmission -> maybeSubmission.orElseThrow(() -> {
                                    throw new ResourceNotFoundException(String.format("Attempted to process submission with ID %s but submission doesn't exist", submissionId));
                                }))
                                .thenCompose(submissionEnvelopeService::processSubmissionAsync)
                                .thenApply(__ -> submissionId)
                                .exceptionally(ex -> {
                                    throw new RuntimeException(String.format("Failed to process submission with id %s", submissionId), ex);
                                });
    }
}