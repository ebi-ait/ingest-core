package org.humancellatlas.ingest.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Javadocs go here!
 *
 * @author tburdett
 * @date 10/09/2017
 */
@RequiredArgsConstructor
@Getter
public abstract class Event {
    private final SubmissionDate submissionDate;
}
