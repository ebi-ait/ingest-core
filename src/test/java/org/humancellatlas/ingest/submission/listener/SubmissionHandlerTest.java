package org.humancellatlas.ingest.submission.listener;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class SubmissionHandlerTest {
    private SubmissionEnvelopeService mockSubmissionEnvelopeService = mock(SubmissionEnvelopeService.class);
    private SubmissionHandler submissionHandler = new SubmissionHandler(mockSubmissionEnvelopeService);

    @Test
    public void testHandleSubmissionProcessingSuccess() throws Exception {
        String mockSubmissionId = "mockSubmissionId";
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        when(mockSubmissionEnvelopeService.getSubmissionById(mockSubmissionId)).thenReturn(Optional.of(submissionEnvelope));
        doNothing().when(mockSubmissionEnvelopeService).processSubmission(submissionEnvelope);

        CompletableFuture<String> f = submissionHandler.handleProcessSubmission(mockSubmissionId);
        Thread.sleep(1000);
        assertThat(f.isDone()).isTrue();
        assertThat(f.isCompletedExceptionally()).isFalse();
        assertThat(f.get()).isEqualTo(mockSubmissionId);
    }

    @Test
    public void testHandleSubmissionProcessingFail() throws Exception {
        String mockSubmissionId = "mockSubmissionId";
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        when(mockSubmissionEnvelopeService.getSubmissionById(mockSubmissionId)).thenReturn(Optional.of(submissionEnvelope));
        doThrow(new RuntimeException()).when(mockSubmissionEnvelopeService).processSubmission(submissionEnvelope);

        CompletableFuture f = submissionHandler.handleProcessSubmission(mockSubmissionId);
        Thread.sleep(1000);
        assertThat(f.isCompletedExceptionally()).isTrue();
    }
}
