package uk.ac.ebi.subs.ingest.core;

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
