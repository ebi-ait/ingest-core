package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/09/17
 */
@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class SubmissionEnvelopeCreateHandler {
  @HandleBeforeCreate
  public boolean submissionEnvelopeBeforeCreate(SubmissionEnvelope submissionEnvelope) {
    this.setUuid(submissionEnvelope);
    return true;
  }

  public SubmissionEnvelope setUuid(SubmissionEnvelope submissionEnvelope) {
    submissionEnvelope.setUuid(Uuid.newUuid());
    return submissionEnvelope;
  }
}
