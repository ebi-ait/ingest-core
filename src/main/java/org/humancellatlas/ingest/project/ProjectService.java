package org.humancellatlas.ingest.project;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class ProjectService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull BundleManifestRepository bundleManifestRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Project addProjectToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Project project) {
        if(! project.getIsUpdate()) {
            return metadataCrudService.addToSubmissionEnvelopeAndSave(project, submissionEnvelope);
        } else {
            return metadataUpdateService.acceptUpdate(project, submissionEnvelope);
        }
    }

    public Project linkProjectSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Project project) {
        final String projectId = project.getId();
        project.addToSubmissionEnvelopes(submissionEnvelope);
        projectRepository.save(project);

        projectRepository.findByUuidUuidAndIsUpdateFalse(project.getUuid().getUuid()).ifPresent(projectByUuid -> {
            if (!projectByUuid.getId().equals(projectId)) {
                projectByUuid.addToSubmissionEnvelopes(submissionEnvelope);
                projectRepository.save(projectByUuid);
            }
        });
        return project;
    }

    public Page<BundleManifest> findBundleManifestsByProjectUuidAndBundleType(Uuid projectUuid, BundleType bundleType, Pageable pageable){
        return this.projectRepository.findByUuidUuidAndIsUpdateFalse(projectUuid.getUuid())
                                     .map(project -> bundleManifestRepository.findBundleManifestsByProjectAndBundleType(project, bundleType, pageable))
                                     .orElseThrow(() -> {
                                         throw new ResourceNotFoundException(String.format("Project with UUID %s not found", projectUuid.getUuid().toString()));
                                     });
    }

    public Page<Project> findByCriteria(List<MetadataCriteria> criteriaList, Boolean andCriteria, Pageable pageable){
        return this.projectRepository.findByCriteria(criteriaList, andCriteria, pageable);
    }

    public Page<SubmissionEnvelope> getProjectSubmissionEnvelopes(Project project, Pageable pageable) {
        Set<SubmissionEnvelope> envelopes = new HashSet<>();
        envelopes.add(project.getSubmissionEnvelope());
        Page<Project> projects = this.projectRepository.findByUuid(project.getUuid(), Pageable.unpaged());
        for (Project projectDocument : projects)
        {
            envelopes.addAll(projectDocument.getSubmissionEnvelopes());
            envelopes.add(projectDocument.getSubmissionEnvelope());
        }
        envelopes.removeIf(Objects::isNull);
        return new PageImpl<>(new ArrayList<>(envelopes), pageable, envelopes.size());
    }
}
