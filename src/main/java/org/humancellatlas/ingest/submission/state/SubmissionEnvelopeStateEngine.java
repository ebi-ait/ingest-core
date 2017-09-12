package org.humancellatlas.ingest.submission.state;

import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionState;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.humancellatlas.ingest.core.ValidationState;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
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
public class SubmissionEnvelopeStateEngine {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull RabbitMessagingTemplate rabbitMessagingTemplate;

    private final @NonNull ExecutorService executorService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired SubmissionEnvelopeStateEngine(SubmissionEnvelopeRepository submissionEnvelopeRepository,
                                             RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.submissionEnvelopeRepository = submissionEnvelopeRepository;
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;

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
        getSubmissionEnvelopeRepository().save(submissionEnvelope);
    }

    private void postMessageIfRequired(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        switch (targetState) {
            case SUBMITTED:
                log.info(String.format("Congratulations! You have submitted your envelope '%s'",
                                       submissionEnvelope.getId()));
                getRabbitMessagingTemplate().convertAndSend(
                        Constants.Exchanges.ENVELOPE_FANOUT,
                        "",
                        new SubmissionEnvelopeMessage(submissionEnvelope));
                break;
            default:
                getLog().debug(
                        String.format("No notification required for state transition to '%s'",
                                      targetState.name()));
        }
    }

    private void postMessageIfRequired(MetadataDocument metadataDocument, ValidationState targetState) {
        getLog().debug(
                String.format("No notification required for state transition to '%s'",
                              targetState.name()));
    }
}
