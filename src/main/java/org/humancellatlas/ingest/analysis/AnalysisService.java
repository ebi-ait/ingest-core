package org.humancellatlas.ingest.analysis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class AnalysisService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull AnalysisRepository analysisRepository;

    public Analysis addAnalysisToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Analysis analysis) {
        Analysis result = getAnalysisRepository().save(analysis);
        getSubmissionEnvelopeRepository().save(submissionEnvelope.addAnalysis(result));
        return result;
    }

}
