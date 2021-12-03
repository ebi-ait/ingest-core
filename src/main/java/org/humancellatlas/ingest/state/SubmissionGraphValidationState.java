package org.humancellatlas.ingest.state;

import java.util.Optional;

public enum SubmissionGraphValidationState {
    PENDING,
    REQUESTED(SubmissionState.DRAFT),
    VALIDATING(SubmissionState.METADATA_VALIDATING),
    VALID(SubmissionState.METADATA_VALID),
    INVALID(SubmissionState.METADATA_INVALID);

    private SubmissionState submissionStateEquivalent;

    SubmissionGraphValidationState(SubmissionState submissionStateEquivalent) {
        // Allow mapping of graph validation state to overall submission state
        // Enables the submission state to reflect the graph validation state
        // Graph validation state is not part of the state tracker
        // We cannot merge graph validation state into submission state since it's needed to track progress of
        // graph validations separately from metadata validation
        this.submissionStateEquivalent = submissionStateEquivalent;
    }

    SubmissionGraphValidationState() { }

    public Optional<SubmissionState> getSubmissionStateEquivalent() {
        return Optional.ofNullable(this.submissionStateEquivalent);
    }
}
