package org.humancellatlas.ingest.errors;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SubmissionErrorService {
    @Autowired
    private SubmissionErrorRepository submissionErrorRepository;

    public Page<SubmissionError> getErrorsFromEnvelope(SubmissionEnvelope submissionEnvelope,
                                                       Pageable pageable) {
        return submissionErrorRepository.findBySubmissionEnvelope(submissionEnvelope, pageable);
    }

    public void addErrorToEnvelope(SubmissionEnvelope submissionEnvelope, SubmissionError submissionError) {
        submissionError.setSubmissionEnvelope(submissionEnvelope);
        submissionErrorRepository.insert(submissionError);
    }
}
