package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Component;

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

    @HandleAfterCreate
    public boolean handleSubmissionEnvelopeCreation(SubmissionEnvelope submissionEnvelope) {
        this.messageRouter.routeStateTrackingNewSubmissionEnvelope(submissionEnvelope);
        this.messageRouter.routeRequestUploadAreaCredentials(submissionEnvelope);
        return true;
    }
}
