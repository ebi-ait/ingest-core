package org.humancellatlas.ingest.submission.state;

import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
        final Event event = new Event(submissionEnvelope.getSubmissionState(), targetState);
        executorService.submit(() -> {
            submissionEnvelope.addEvent(event).enactStateTransition(targetState);

            // is this an event that needs to be posted to a queue?
            postMessageIfRequired(submissionEnvelope, targetState);
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
}
