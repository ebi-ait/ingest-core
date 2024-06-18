package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Component;

import lombok.NonNull;
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
  private final @NonNull MessageRouter messageRouter;
  private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

  private final @NonNull ResourceMappings mappings;
  private final @NonNull RepositoryRestConfiguration config;
  private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

  @HandleBeforeCreate
  public boolean submissionEnvelopeBeforeCreate(SubmissionEnvelope submissionEnvelope) {
    this.setUuid(submissionEnvelope);
    return true;
  }

  public SubmissionEnvelope setUuid(SubmissionEnvelope submissionEnvelope) {
    submissionEnvelope.setUuid(Uuid.newUuid());
    return submissionEnvelope;
  }

  @HandleAfterCreate
  public boolean handleSubmissionEnvelopeCreationEvent(SubmissionEnvelope submissionEnvelope) {
    return this.handleSubmissionEnvelopeCreation(submissionEnvelope);
  }

  public boolean handleSubmissionEnvelopeCreation(SubmissionEnvelope submissionEnvelope) {
    this.messageRouter.routeStateTrackingNewSubmissionEnvelope(submissionEnvelope);
    this.messageRouter.routeRequestUploadAreaCredentials(submissionEnvelope);
    log.info(
        String.format("Submission envelope with ID %s was created.", submissionEnvelope.getId()));
    return true;
  }
}
