package org.humancellatlas.ingest.errors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionErrorService {
    @NonNull
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    public List<SubmissionError> getErrorFromEnvelope(SubmissionEnvelope submissionEnvelope) {
        return submissionEnvelope.getSubmissionErrors();
    }

    public SubmissionEnvelope addErrorToEnvelope(SubmissionEnvelope submissionEnvelope, SubmissionError submissionError) {
        submissionEnvelope.addError(submissionError);
        return submissionEnvelopeRepository.save(submissionEnvelope);
    }
}
