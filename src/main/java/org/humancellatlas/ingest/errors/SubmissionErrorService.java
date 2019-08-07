package org.humancellatlas.ingest.errors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionErrorService {
    @NonNull
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    public SubmissionEnvelope addErrorToEnvelope(SubmissionError submissionError, SubmissionEnvelope submissionEnvelope) {
        submissionEnvelope.addError(submissionError);
        return submissionEnvelopeRepository.save(submissionEnvelope);
    }
}
