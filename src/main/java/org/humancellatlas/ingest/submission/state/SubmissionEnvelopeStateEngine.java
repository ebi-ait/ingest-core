package org.humancellatlas.ingest.submission.state;

import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionState;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 10/09/17
 */
public class SubmissionEnvelopeStateEngine {
    public static String getRelNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return Links.DRAFT_REL;
            case VALIDATING:
                return Links.VALIDATING_REL;
            case VALID:
                return Links.VALID_REL;
            case INVALID:
                return Links.INVALID_REL;
            case SUBMITTED:
                return Links.SUBMIT_REL;
            case PROCESSING:
                return Links.PROCESSING_REL;
            case CLEANUP:
                return "mark-cleaning";
            case COMPLETE:
                return "mark-complete";
            default:
                throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                        "as a submission envelope state that can be set", submissionState.name()));
        }
    }

    public static String getSubresourceNameForSubmissionState(SubmissionState submissionState) {
        switch (submissionState) {
            case DRAFT:
                return "/draftState";
            case VALIDATING:
                return "/validatingState";
            case VALID:
                return "/validState";
            case INVALID:
                return "/invalidState";
            case SUBMITTED:
                return "/submittedState";
            case PROCESSING:
                return "/processingState";
            case CLEANUP:
                return "/cleanupState";
            case COMPLETE:
                return "/completeState";
            default:
                throw new InvalidSubmissionStateException(String.format("The submission state '%s' is not recognised " +
                        "as a submission envelope state that can be set", submissionState.name()));
        }
    }
}
