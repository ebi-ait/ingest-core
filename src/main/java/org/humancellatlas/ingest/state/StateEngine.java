package org.humancellatlas.ingest.state;

import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessage;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessageBuilder;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 10/09/17
 */
@Service
@Getter
public class StateEngine {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    private final @NonNull ResourceMappings mappings;
    private final @NonNull RepositoryRestConfiguration config;

    private final @NonNull ExecutorService executorService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired StateEngine(SubmissionEnvelopeRepository submissionEnvelopeRepository,
                           RabbitMessagingTemplate rabbitMessagingTemplate,
                           ResourceMappings mappings,
                           RepositoryRestConfiguration config) {
        this.submissionEnvelopeRepository = submissionEnvelopeRepository;
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;

        this.mappings = mappings;
        this.config = config;

        this.executorService = Executors.newCachedThreadPool();
    }

    @PreDestroy
    void shutdownExecutor() {
        getLog().info("Shutting down state engine...");
        this.executorService.shutdown();
        getLog().info("State engine shutdown successfully");
    }

    public Event advanceStateOfEnvelope(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        if (!submissionEnvelope.allowedStateTransitions().contains(targetState)) {
            throw new IllegalStateException(String.format(
                    "It is not possible to transition envelope '%s' to the state '%s'",
                    submissionEnvelope.getId(),
                    targetState));
        }

        final Event event = new SubmissionEvent(submissionEnvelope.getSubmissionState(), targetState);
        executorService.submit(() -> {
            submissionEnvelope.addEvent(event).enactStateTransition(targetState);

            getSubmissionEnvelopeRepository().save(submissionEnvelope);

            // is this an event that needs to be posted to a queue?
            postMessageIfRequired(submissionEnvelope, targetState);
        });
        return event;
    }

    public <S extends MetadataDocument, T extends MongoRepository<S, String>>
    Event advanceStateOfMetadataDocument(T repository, S metadataDocument, ValidationState targetState) {
        if (!metadataDocument.allowedStateTransitions().contains(targetState)) {
            throw new IllegalStateException(String.format(
                    "It is not possible to transition metadata document '%s' to the state '%s'",
                    metadataDocument.getId(),
                    targetState));
        }

        final Event event = new ValidationEvent(metadataDocument.getValidationState(), targetState);
        executorService.submit(() -> {
            metadataDocument.addEvent(event).enactStateTransition(targetState);

            repository.save(metadataDocument);

            // is this an event that needs to be posted to a queue?
            postMessageIfRequired(metadataDocument, targetState);
        });
        return event;

    }

    public Optional<Event> analyseStateOfEnvelope(SubmissionEnvelope submissionEnvelope) {
        SubmissionState determinedState = submissionEnvelope.determineEnvelopeState();
        if (submissionEnvelope.getSubmissionState().equals(determinedState)) {
            return Optional.empty();
        }
        else {
            return Optional.of(advanceStateOfEnvelope(submissionEnvelope, determinedState));
        }
    }

    public void notifySubmissionEnvelopeOfMetadataDocumentChange(SubmissionEnvelope submissionEnvelope,
                                                                 MetadataDocument metadataDocument) {
        submissionEnvelope.notifyOfMetadataDocumentState(metadataDocument);
        postMessageIfRequired(metadataDocument, metadataDocument.getValidationState());
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
    }

    private void postMessageIfRequired(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        switch (targetState) {
            case SUBMITTED:
                log.info(String.format("Congratulations! You have submitted your envelope '%s'",
                                       submissionEnvelope.getId()));
                SubmissionEnvelopeMessage submissionMessage =
                        SubmissionEnvelopeMessageBuilder.using(mappings, config).messageFor(submissionEnvelope).build();

                getRabbitMessagingTemplate().convertAndSend(
                        Constants.Exchanges.ENVELOPE_FANOUT,
                        "",
                        submissionMessage);
                break;
            default:
                getLog().debug(
                        String.format("No notification required for state transition to '%s'",
                                      targetState.name()));
        }
    }

    private void postMessageIfRequired(MetadataDocument metadataDocument, ValidationState targetState) {
        switch (targetState) {
            case DRAFT:
                getLog().info(String.format(
                        "Metadata document '%s' has been put into a draft state... notifying validation service",
                        metadataDocument.getId()));
                MetadataDocumentMessage validationMessage =
                        MetadataDocumentMessageBuilder.using(mappings, config).messageFor(metadataDocument).build();
                getRabbitMessagingTemplate().convertAndSend(Constants.Exchanges.VALIDATION_FANOUT,
                                                            "",
                                                            validationMessage);
                break;
            case VALID:
                getLog().info(String.format(
                        "Metadata document '%s' has been put into a valid state... notifying accessioning service",
                        metadataDocument.getId()));
                MetadataDocumentMessage accessioningMessage =
                        MetadataDocumentMessageBuilder.using(mappings, config).messageFor(metadataDocument).build();
                getRabbitMessagingTemplate().convertAndSend(Constants.Exchanges.ACCESSION_FANOUT,
                                                            "",
                                                            accessioningMessage);
                break;
            default:
                getLog().debug(
                        String.format("No notification required for state transition to '%s'",
                                      targetState.name()));
        }
    }
}
