package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.patch.PatchRepository;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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
    private final MetadataUpdateService metadataUpdateService;

    @NonNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @NonNull
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @NonNull
    private final SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

    @NonNull
    private final SubmissionManifestRepository submissionManifestRepository;
    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());
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

    public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope,
                                                 SubmissionState state) {
        if (!envelope.allowedStateTransitions().contains(state)) {
            throw new StateTransitionNotAllowed(String.format(
                    "Envelope with id %s cannot be transitioned from state %s to state %s",
                    envelope.getId(), envelope.getSubmissionState(), state));
        } else {
            messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);
        }
    }

    public void handleSubmissionRequest(SubmissionEnvelope envelope) {
        if (!envelope.getIsUpdate()) {
            handleSubmitOriginalSubmission(envelope);
        } else {
            handleSubmitUpdateSubmission(envelope);
        }
    }

    private void handleSubmitOriginalSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                exporter.exportBundles(submissionEnvelope);
            } catch (Exception e) {
                log.error("Uncaught Exception exporting Bundles", e);
            }
        });
    }

    private void handleSubmitUpdateSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                metadataUpdateService.applyUpdates(submissionEnvelope);
                exporter.updateBundles(submissionEnvelope);
            } catch (Exception e) {
                log.error("Uncaught Exception Applying Updates or Exporting Bundles", e);
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

        this.cleanupLinksToSubmissionMetadata(submissionEnvelope);

        biomaterialRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        processRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        protocolRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        fileRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        bundleManifestRepository.deleteByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString());
        patchRepository.deleteBySubmissionEnvelope(submissionEnvelope);
        submissionManifestRepository.deleteBySubmissionEnvelope(submissionEnvelope);

        //When a submission envelope can only have one project this for loop can be removed.
        Page<Project> projects = projectRepository.findBySubmissionEnvelope(submissionEnvelope, Pageable.unpaged());
        for (Project project : projects) {
            project.removeSubmissionEnvelopeData(submissionEnvelope, forceDelete);
            projectRepository.save(project);
        }
        submissionEnvelopeRepository.delete(submissionEnvelope);
    }


    /**
     *
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

        long endTime = System.currentTimeMillis();
        float duration = ((float)(endTime - startTime)) / 1000;
        String durationStr = new DecimalFormat("#,###.##").format(duration);
        log.info("cleanup link time: {} s", durationStr);
    }
}
