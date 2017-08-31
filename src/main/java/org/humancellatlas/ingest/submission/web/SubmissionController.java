package org.humancellatlas.ingest.submission.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionService;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Component
@RequiredArgsConstructor
public class SubmissionController {
    private final @NonNull SubmissionService submissionService;
}
