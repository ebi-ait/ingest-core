package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.exception.StateTransitionNotAllowed;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.export.Exporter;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifest;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

    public void handleEnvelopeStateUpdateRequest(SubmissionEnvelope envelope,
            SubmissionState state) {
        if(! envelope.allowedStateTransitions().contains(state)) {
            throw new StateTransitionNotAllowed(String.format(
                    "Envelope with id %s cannot be transitioned from state %s to state %s",
                    envelope.getId(), envelope.getSubmissionState(), state));
        } else {
            messageRouter.routeStateTrackingUpdateMessageForEnvelopeEvent(envelope, state);
        }
    }

    public void handleSubmissionRequest(SubmissionEnvelope envelope) {
        if(! envelope.getIsUpdate()) {
            handleSubmitOriginalSubmission(envelope);
        } else {
            handleSubmitUpdateSubmission(envelope);
        }
    }

    private void handleSubmitOriginalSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                exporter.exportBundles(submissionEnvelope);
            }
            catch (Exception e) {
                log.error("Uncaught Exception exporting Bundles", e);
            }
        });
    }

    private void handleSubmitUpdateSubmission(SubmissionEnvelope submissionEnvelope) {
        executorService.submit(() -> {
            try {
                metadataUpdateService.applyUpdates(submissionEnvelope);
                exporter.updateBundles(submissionEnvelope);
            }
            catch (Exception e) {
                log.error("Uncaught Exception Applying Updates or Exporting Bundles", e);
            }
        });
    }

    public SubmissionEnvelope createUpdateSubmissionEnvelope() {
        SubmissionEnvelope updateSubmissionEnvelope = new SubmissionEnvelope();
        submissionEnvelopeCreateHandler.setUuid(updateSubmissionEnvelope);
        updateSubmissionEnvelope.setIsUpdate(true);
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = submissionEnvelopeRepository.insert(updateSubmissionEnvelope);
        submissionEnvelopeCreateHandler.handleSubmissionEnvelopeCreation(updateSubmissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }

    public void deleteSubmission(SubmissionEnvelope submissionEnvelope){
        SubmissionState state = submissionEnvelope.getSubmissionState();
        if(state == SubmissionState.COMPLETE || state == SubmissionState.CLEANUP)
            throw new UnsupportedOperationException("Cannot delete submission if it is Complete or in Cleanup.");

        SubmissionManifest submissionManifest = submissionManifestRepository.findBySubmissionEnvelopeId(submissionEnvelope.getId());
        Page<Biomaterial> biomaterials = biomaterialRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, Pageable.unpaged());
        Page<Process> processes = processRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, Pageable.unpaged());
        Page<File> files = fileRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, Pageable.unpaged());
        Page<Protocol> protocols = protocolRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, Pageable.unpaged());
        Page<BundleManifest> bundleManifests = bundleManifestRepository.findByEnvelopeUuid(submissionEnvelope.getUuid().toString(), Pageable.unpaged());

        if (!biomaterials.isEmpty())
            biomaterialRepository.deleteAll(biomaterials);
        if (!processes.isEmpty())
            processRepository.deleteAll(processes);
        if (!protocols.isEmpty())
            protocolRepository.deleteAll(protocols);
        if (!files.isEmpty())
            fileRepository.deleteAll(files);
        if (!bundleManifests.isEmpty())
            bundleManifestRepository.deleteAll(bundleManifests);
        if (submissionManifest != null)
            submissionManifestRepository.delete(submissionManifest);

        submissionEnvelopeRepository.delete(submissionEnvelope);
    }
}
