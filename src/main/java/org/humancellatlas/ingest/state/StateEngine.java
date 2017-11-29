package org.humancellatlas.ingest.state;

import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessage;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.ValidationEvent;
import org.humancellatlas.ingest.messaging.Constants;
import org.humancellatlas.ingest.messaging.MessageSender;
import org.humancellatlas.ingest.submission.StatePropagationException;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessageBuilder;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.humancellatlas.ingest.submission.SubmissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private final @NonNull MessageSender messageSender;
    private final @NonNull ResourceMappings mappings;
    private final @NonNull RepositoryRestConfiguration config;

    private final @NonNull ExecutorService executorService;

    private static final int MAX_RETRIES = 25;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public StateEngine(SubmissionEnvelopeRepository submissionEnvelopeRepository,
                       MessageSender messageSender,
                       ResourceMappings mappings,
                       RepositoryRestConfiguration config) {
        this.submissionEnvelopeRepository = submissionEnvelopeRepository;
        this.messageSender = messageSender;
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
            StringBuilder msgBuilder = new StringBuilder();
            msgBuilder.append("\n\nState report for envelope ").append(submissionEnvelope.getId()).append(":\n");
            int trackedDocs = submissionEnvelope.getValidationStateMap().size();
            msgBuilder.append("\tTracking ").append(trackedDocs).append(" documents\n");
            msgBuilder.append("\tAll states: {\n");
            for (Map.Entry<String, ValidationState> entry : submissionEnvelope.getValidationStateMap().entrySet()) {
                msgBuilder.append("\t\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            msgBuilder.append("\t}\n");

            throw new IllegalStateException(String.format(
                    "It is not possible to transition envelope '%s' from state '%s' to state '%s'\n%s",
                    submissionEnvelope.getId(),
                    submissionEnvelope.getSubmissionState(),
                    targetState,
                    msgBuilder.toString()));
        }

        final Event event = new SubmissionEvent(submissionEnvelope.getSubmissionState(), targetState);
        executorService.submit(() -> {
            // we'll retry events here if they fail
            int tries = 0;
            Exception lastException = null;
            SubmissionEnvelope envelope = submissionEnvelope;
            while (tries < MAX_RETRIES) {
                tries++;
                try {
                    envelope.addEvent(event).enactStateTransition(targetState);

                    getSubmissionEnvelopeRepository().save(envelope);

                    // is this an event that needs to be posted to a queue?
                    postMessageIfRequired(envelope, targetState);
                    return;
                }
                catch (Exception e) {
                    lastException = e;
                    getLog().trace("Exception on envelope operation", e);
                    getLog().debug(String.format(
                            "Encountered exception whilst running submission envelope operation... " +
                                    "will reattempt (tries now = %s)",
                            tries));
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    }
                    catch (InterruptedException e1) {
                        // just continue
                    }
                    // refresh submission envelope
                    envelope = getSubmissionEnvelopeRepository().findOne(submissionEnvelope.getId());
                }
            }
            throw new StatePropagationException(
                    "Critical error - failed to run submission envelope operation after multiple attempts!",
                    lastException);
        });
        return event;
    }

    public <S extends MetadataDocument, T extends MongoRepository<S, String>>
    Event advanceStateOfMetadataDocument(T repository, S metadataDocument, ValidationState targetState) {
        if (!metadataDocument.allowedStateTransitions().contains(targetState)) {
            throw new IllegalStateException(String.format(
                    "It is not possible to transition document '%s: %s' from state '%s' to state '%s'",
                    metadataDocument.getClass().getSimpleName(),
                    metadataDocument.getId(),
                    metadataDocument.getValidationState(),
                    targetState));
        }

        final Event event = new ValidationEvent(metadataDocument.getValidationState(), targetState);
        metadataDocument.addEvent(event).enactStateTransition(targetState);

        repository.save(metadataDocument);

        // is this an event that needs to be posted to a queue?
        postMessageIfRequired(metadataDocument, targetState);
        return event;
    }

    public Optional<Event> analyseStateOfEnvelope(SubmissionEnvelope submissionEnvelope) {
        // we'll retry events here if they fail
        int tries = 0;
        Exception lastException = null;
        SubmissionEnvelope envelope = submissionEnvelope;
        while (tries < MAX_RETRIES) {
            tries++;
            try {
                SubmissionState determinedState = envelope.determineEnvelopeState();
                // state map cleaned but not saved
                if (envelope.getSubmissionState().equals(determinedState)) {
                    // save to flush any state map updates
                    getSubmissionEnvelopeRepository().save(envelope);
                    return Optional.empty();
                }
                else {
                    return Optional.of(advanceStateOfEnvelope(envelope, determinedState));
                }
            }
            catch (Exception e) {
                lastException = e;
                getLog().trace("Exception on metadata operation", e);
                getLog().debug(String.format(
                        "Encountered exception whilst analysing envelope state... " +
                                "will reattempt (tries now = %s)",
                        tries));
                try {
                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException e1) {
                    // just continue
                }
                // refresh submission envelope
                envelope = getSubmissionEnvelopeRepository().findOne(submissionEnvelope.getId());
            }
        }
        throw new StatePropagationException(
                "Critical error - failed to run metadata document operation after multiple attempts!",
                lastException);
    }

    public SubmissionEnvelope notifySubmissionEnvelopeOfMetadataDocumentChange(SubmissionEnvelope submissionEnvelope,
                                                                 MetadataDocument metadataDocument) {
        // we'll retry events here if they fail
        int tries = 0;
        Exception lastException = null;
        SubmissionEnvelope envelope = submissionEnvelope;
        while (tries < MAX_RETRIES) {
            tries++;
            try {
                postMessageIfRequired(metadataDocument, metadataDocument.getValidationState());
                if (envelope.flagPossibleMetadataDocumentStateChange(metadataDocument)) {
                    return getSubmissionEnvelopeRepository().save(envelope);
                }
                else {
                    return envelope;
                }
            }
            catch (Exception e) {
                lastException = e;
                getLog().trace("Exception on metadata operation", e);
                getLog().debug(String.format(
                        "Encountered exception whilst updating submission envelope of metadata change... " +
                                "will reattempt (tries now = %s)",
                        tries));
                try {
                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException e1) {
                    // just continue
                }
                // refresh submission envelope
                envelope = getSubmissionEnvelopeRepository().findOne(submissionEnvelope.getId());
            }
        }
        throw new StatePropagationException(
                "Critical error - failed to run metadata document operation after multiple attempts!",
                lastException);
    }

    private void postMessageIfRequired(SubmissionEnvelope submissionEnvelope, SubmissionState targetState) {
        SubmissionEnvelopeMessage submissionMessage =
                SubmissionEnvelopeMessageBuilder.using(mappings, config).messageFor(submissionEnvelope).build();

        switch (targetState) {
            case SUBMITTED:
                log.info(String.format("Congratulations! You have submitted your envelope '%s'",
                                       submissionEnvelope.getId()));

                getMessageSender().queueExportMessage(
                        Constants.Exchanges.ENVELOPE_SUBMITTED_FANOUT,
                        "",
                        submissionMessage);
                break;
            case COMPLETE:
                log.info(String.format("Congratulations! You have exported bundle to DSS '%s'",
                        submissionEnvelope.getId()));

                getMessageSender().queueExportMessage(
                        Constants.Exchanges.SUBMISSION_ARCHIVAL_DIRECT,
                        Constants.Queues.SUBMISSION_ARCHIVAL,
                        submissionMessage);
                break;
            default:
                getLog().debug(
                        String.format("No notification required for state transition to '%s'",
                                      targetState.name()));
        }
    }

    private void postMessageIfRequired(MetadataDocument metadataDocument, ValidationState targetState) {
        MetadataDocumentMessage message =
                MetadataDocumentMessageBuilder.using(mappings, config).messageFor(metadataDocument).build();

        switch (targetState) {
            case DRAFT:
                if (metadataDocument.getUuid() == null) {
                    getLog().debug(String.format(
                            "Draft metadata document '%s: %s' has no uuid... notifying accessioning service",
                            metadataDocument.getClass().getSimpleName(), metadataDocument.getId()));
                    getMessageSender().queueAccessionMessage(Constants.Exchanges.ACCESSION,
                                                             Constants.Queues.ACCESSION_REQUIRED,
                                                             message);
                }

                getLog().debug(String.format(
                        "Metadata document '%s: %s' has been put into a draft state... notifying validation service",
                        metadataDocument.getClass().getSimpleName(), metadataDocument.getId()));
                getMessageSender().queueValidationMessage(Constants.Exchanges.VALIDATION,
                                                          Constants.Queues.VALIDATION_REQUIRED,
                                                          message);
                break;
            default:
                getLog().debug(
                        String.format("No notification required for metadata document '%s: %s' state transition to '%s'",
                                      metadataDocument.getClass().getSimpleName(),
                                      metadataDocument.getId(),
                                      targetState.name()));
        }
    }
}
