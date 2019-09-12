package org.humancellatlas.ingest.submission.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class SubmissionHandler {
    private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;
    private final @NonNull ExecutorService executorService = Executors.newFixedThreadPool(20);

    public CompletableFuture<String> handleProcessSubmission(String submissionId) {
        return CompletableFuture.supplyAsync(() -> submissionEnvelopeService.getSubmissionById(submissionId), executorService)
                                .thenApply(maybeSubmission -> maybeSubmission.orElseThrow(() -> {
                                    throw new ResourceNotFoundException(String.format("Attempted to process submission with ID %s but submission doesn't exist", submissionId));
                                }))
                                .thenAcceptAsync(submissionEnvelopeService::processSubmission, executorService)
                                .thenApply(__ -> submissionId)
                                .exceptionally(ex -> {
                                    throw new RuntimeException(String.format("Failed to process submission with id %s", submissionId), ex);
                                });
    }
}