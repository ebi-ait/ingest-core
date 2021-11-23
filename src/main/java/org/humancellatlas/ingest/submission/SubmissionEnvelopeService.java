package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.errors.SubmissionErrorRepository;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.patch.PatchRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionGraphValidationState;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.SubmitAction;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SubmissionEnvelopeService {

    @NonNull
    private final MessageRouter messageRouter;

    @NonNull
    private final Exporter exporter;

    @NonNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @NonNull
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @NonNull
    private final SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

    @NonNull
    private final SubmissionManifestRepository submissionManifestRepository;

    @NonNull
    private final Logger log = LoggerFactory.getLogger(getClass());

    @NonNull
    private BundleManifestRepository bundleManifestRepository;

    @NonNull
    private ProjectRepository projectRepository;

    @NonNull
    private ProcessRepository processRepository;

    @NonNull
    private ProtocolRepository protocolRepository;

    @NonNull
    private FileRepository fileRepository;

    @NonNull
    private BiomaterialRepository biomaterialRepository;

    @NonNull
    private PatchRepository patchRepository;

    @NonNull
    private SubmissionErrorRepository submissionErrorRepository;

    public void handleSubmitRequest(SubmissionEnvelope envelope, List<SubmitAction> submitActions) {
        if(envelope.getGraphValidationState() != SubmissionGraphValidationState.VALID) {
            throw new RuntimeException((String.format(
                    "Envelope with id %s cannot be submitted without a valid graphValidationState",
                    envelope.getId()
            )));
        }

        if (isSubmitAction(submitActions)) {
            envelope.setSubmitActions(new HashSet<>(submitActions));
            submissionEnvelopeRepository.save(envelope);
        } else {
            throw new IllegalArgumentException((String.format(
                    "Envelope with id %s is submitted without the required submit actions",
                    envelope.getId(), envelope.getSubmissionState())));
        }
        handleEnvelopeStateUpdateRequest(envelope, SubmissionState.SUBMITTED);
    }

    public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope,
                                                 SubmissionState state) {
        if (!envelope.allowedSubmissionStateTransitions().contains(state)) {
            throw new StateTransitionNotAllowed(String.format(
                    "Envelope with id %s cannot be transitioned from state %s to state %s",
                    envelope.getId(), envelope.getSubmissionState(), state));
        } else {
            messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);
        }
    }

    public void handleGraphValidationStateUpdateRequest(SubmissionEnvelope envelope,
                                                        SubmissionGraphValidationState state) {
        if (!envelope.allowedGraphValidationStateTransitions().contains(state)) {
            throw new RuntimeException(String.format(
                    "Envelope with id %s cannot be transitioned from graphValidationState %s to graphValidationState %s",
                    envelope.getId(), envelope.getGraphValidationState(), state));
        } else {
            Boolean wasInvalid = envelope.getGraphValidationState() == SubmissionGraphValidationState.INVALID;
            envelope.enactGraphValidationStateTransition(state);
            submissionEnvelopeRepository.save(envelope);

            if(wasInvalid) {
                removeGraphValidationErrors(envelope);
            }
        }
    }

    public void handleCommitSubmit(SubmissionEnvelope envelope) {
        Set<SubmitAction> submitActions = envelope.getSubmitActions();
        if (submitActions.contains(SubmitAction.ARCHIVE)) {
            archiveSubmission(envelope);
        } else if (shouldExport(submitActions)) {
            exportSubmission(envelope);
        } else {
            throw new RuntimeException((String.format(
                    "Envelope with id %s is submitted without the required submit actions",
                    envelope.getId(), envelope.getSubmissionState())));
        }
    }

    private void archiveSubmission(SubmissionEnvelope envelope) {
            exporter.exportManifests(envelope);
    }

    public void exportSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                exporter.exportProcesses(submissionEnvelope);
            } catch (Exception e) {
                log.error("Uncaught Exception exporting Bundles", e);
            }
        });
    }

    public void handleCommitArchived(SubmissionEnvelope envelope) {
        if (envelope.getSubmitActions().contains(SubmitAction.EXPORT)) {
            exportSubmission(envelope);
        }
    }

    public void handleCommitExported(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                if (submissionEnvelope.getSubmitActions().contains(SubmitAction.CLEANUP)) {
                    handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.CLEANUP);
                }
            } catch (Exception e) {
                log.error("Uncaught Exception exporting Bundles", e);
            }
        });
    }

    public SubmissionEnvelope createUpdateSubmissionEnvelope() {
        SubmissionEnvelope updateSubmissionEnvelope = new SubmissionEnvelope();
        submissionEnvelopeCreateHandler.setUuid(updateSubmissionEnvelope);
        updateSubmissionEnvelope.setIsUpdate(true);
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = createSubmissionEnvelope(updateSubmissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }

    public SubmissionEnvelope createSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        SubmissionEnvelope insertedSubmissionEnvelope = submissionEnvelopeRepository.insert(submissionEnvelope);
        submissionEnvelopeCreateHandler.handleSubmissionEnvelopeCreation(submissionEnvelope);
        return insertedSubmissionEnvelope;
    }

    public void deleteSubmission(SubmissionEnvelope submissionEnvelope, boolean forceDelete) {
        if (!(submissionEnvelope.isOpen() || forceDelete))
            throw new UnsupportedOperationException("Cannot delete submission if it is already submitted!");

        RetryTemplate retry = RetryTemplate.builder()
                .maxAttempts(5)
                .fixedBackoff(75)
                .retryOn(OptimisticLockingFailureException.class)
                .build();
        retry.execute(context -> {
            cleanupLinksToSubmissionMetadata(submissionEnvelope);
            return null;
        });

        biomaterialRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        processRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        protocolRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        fileRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        bundleManifestRepository.deleteByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString());
        patchRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        submissionManifestRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        submissionErrorRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        submissionEnvelopeRepository.delete(submissionEnvelope);
    }


    /**
     * Ensures that any links to metadata in the submission are removed.
     *
     * @param submissionEnvelope
     */
    private void cleanupLinksToSubmissionMetadata(SubmissionEnvelope submissionEnvelope) {
        long startTime = System.currentTimeMillis();

        processRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(p -> {
                    fileRepository.findByInputToProcessesContains(p)
                            .forEach(file -> {
                                file.getInputToProcesses().remove(p);
                                fileRepository.save(file);
                            });

                    fileRepository.findByDerivedByProcessesContains(p)
                            .forEach(file -> {
                                file.getDerivedByProcesses().remove(p);
                                fileRepository.save(file);
                            });

                    biomaterialRepository.findByInputToProcessesContains(p)
                            .forEach(biomaterial -> {
                                biomaterial.getInputToProcesses().remove(p);
                                biomaterialRepository.save(biomaterial);
                            });

                    biomaterialRepository.findByDerivedByProcessesContains(p)
                            .forEach(biomaterial -> {
                                biomaterial.getDerivedByProcesses().remove(p);
                                biomaterialRepository.save(biomaterial);
                            });
                });

        protocolRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(protocol -> processRepository.findByProtocolsContains(protocol)
                        .forEach(process -> {
                            process.getProtocols().remove(protocol);
                            processRepository.save(process);
                        }));

        bundleManifestRepository.findByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString())
                .forEach(bundleManifest -> processRepository.findByInputBundleManifestsContains(bundleManifest)
                        .forEach(process -> {
                            process.getInputBundleManifests().remove(bundleManifest);
                            processRepository.save(process);
                        }));

        fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(file -> projectRepository.findBySupplementaryFilesContains(file)
                        .forEach(project -> {
                            project.getSupplementaryFiles().remove(file);
                            projectRepository.save(project);
                        }));

        // project cleanup

        projectRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(project -> {
                    project.setSubmissionEnvelope(null); // TODO: address this; we should implement project containers that aren't deleted as part of deleteSubmission()
                    projectRepository.save(project);
                });

        projectRepository.findBySubmissionEnvelopesContains(submissionEnvelope)
                .forEach(project -> {
                    project.getSubmissionEnvelopes().remove(submissionEnvelope);
                    projectRepository.save(project);
                });

        long endTime = System.currentTimeMillis();
        float duration = ((float) (endTime - startTime)) / 1000;
        String durationStr = new DecimalFormat("#,###.##").format(duration);
        log.info("cleanup link time: {} s", durationStr);
    }

    private boolean isSubmitAction(List<SubmitAction> submitActions) {
        return submitActions.contains(SubmitAction.ARCHIVE)
                || submitActions.contains(SubmitAction.EXPORT)
                || submitActions.contains(SubmitAction.EXPORT_METADATA);
    }

    private boolean shouldExport(Set<SubmitAction> submitActions) {
        return submitActions.contains(SubmitAction.EXPORT) || submitActions.contains(SubmitAction.EXPORT_METADATA);
    }

    private void removeGraphValidationErrors(SubmissionEnvelope submissionEnvelope) {
        biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(biomaterial -> {
                    biomaterial.setGraphValidationErrors(new ArrayList<>());
                    biomaterialRepository.save(biomaterial);
                });

        processRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(process -> {
                    process.setGraphValidationErrors(new ArrayList<>());
                    processRepository.save(process);
                });

        protocolRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(protocol -> {
                    protocol.setGraphValidationErrors(new ArrayList<>());
                    protocolRepository.save(protocol);
                });

        fileRepository.findBySubmissionEnvelope(submissionEnvelope)
                .forEach(file -> {
                    file.setGraphValidationErrors(new ArrayList<>());
                    fileRepository.save(file);
                });
    }
}
