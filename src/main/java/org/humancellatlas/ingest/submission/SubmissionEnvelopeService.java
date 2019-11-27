package org.humancellatlas.ingest.submission;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.exception.LinkToNewSubmissionNotAllowedException;
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

import java.util.Optional;
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

    @NonNull
    private PatchRepository patchRepository;


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
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = createSubmissionEnvelope(updateSubmissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }

    public SubmissionEnvelope createSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        SubmissionEnvelope insertedUpdateSubmissionEnvelope = submissionEnvelopeRepository.insert(submissionEnvelope);
        submissionEnvelopeCreateHandler.handleSubmissionEnvelopeCreation(submissionEnvelope);
        return insertedUpdateSubmissionEnvelope;
    }

    public SubmissionEnvelope createSubmissionAndLinkToProject(SubmissionEnvelope envelope, Project project){
        assertProjectIsNotOpen(project);
        SubmissionEnvelope submissionEnvelope = createSubmissionEnvelope(envelope);
        project.addToSubmissionEnvelopes(submissionEnvelope);
        projectRepository.save(project);
        return submissionEnvelope;
    }

    private void assertProjectIsNotOpen(Project project){
        Optional<SubmissionEnvelope> openSubmission = Optional.ofNullable(project.getOpenSubmissionEnvelope());
        if (openSubmission.isPresent()) {
            throw new LinkToNewSubmissionNotAllowedException(String.format("The project is still linked to an open submission envelope %s", openSubmission.get().getUuid().toString()));
        }
    }

    public SubmissionEnvelope linkSubmissionToProject(SubmissionEnvelope envelope, Project project){
        assertProjectIsNotOpen(project);
        project.addToSubmissionEnvelopes(envelope);
        projectRepository.save(project);
        return envelope;
    }


    public void deleteSubmission(SubmissionEnvelope submissionEnvelope, boolean forceDelete){
        if(!(submissionEnvelope.isOpen() || forceDelete))
            throw new UnsupportedOperationException("Cannot delete submission if it is already submitted!");

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
}
