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
import org.humancellatlas.ingest.query.Operator;
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
import java.util.List;
import java.util.Optional;


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

    public Page<BundleManifest> findBundleManifestsByProjectUuidAndBundleType(Uuid projectUuid, BundleType bundleType, Pageable pageable){
        return this.projectRepository.findByUuidUuidAndIsUpdateFalse(projectUuid.getUuid())
                                     .map(project -> bundleManifestRepository.findBundleManifestsByProjectAndBundleType(project, bundleType, pageable))
                                     .orElseThrow(() -> {
                                         throw new ResourceNotFoundException(String.format("Project with UUID %s not found", projectUuid.getUuid().toString()));
                                     });
    }

    public Page<Project> queryByContent(List<MetadataCriteria> query, Optional<Boolean> isUpdate, Pageable pageable){
        return this.projectRepository.findByContent(query, isUpdate, pageable);
    }

    public Page<SubmissionEnvelope> getProjectSubmissionEnvelopes(Project project, Pageable pageable) {
        Page<Project> projects = this.projectRepository.findByUuid(project.getUuid(), Pageable.unpaged());
        List<SubmissionEnvelope> envelopes = new ArrayList<>();
        for (Project projectDocument : projects)
        {
            envelopes.addAll(projectDocument.getSubmissionEnvelopes());
        }
        return new PageImpl<>(envelopes, pageable, envelopes.size());
    }

    public Page<ProjectSubmissions> getMissingSubmissionEnvelopes(Pageable pageable) {
        List<ProjectSubmissions> missingProjectSubmissions = new ArrayList<>();
        List<String> bannedTitles = new ArrayList<>();
        bannedTitles.add("SS2 1 Cell Integration Test");
        bannedTitles.add( "10x 1 Run Integration Test");
        bannedTitles.add("");

        MetadataCriteria criteria = new MetadataCriteria();
        criteria.setContentField("project_core.project_title");
        criteria.setOperator(Operator.NIN);
        criteria.setValue(bannedTitles);

        List<MetadataCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        Optional<Boolean> isUpdate = Optional.of(false);

        for (Project project : this.projectRepository.findByContent(criteriaList, isUpdate, Pageable.unpaged())) {
            ProjectSubmissions projectSubmissions = new ProjectSubmissions(project);
            Page<Project> projects = this.projectRepository.findByUuid(project.getUuid(), Pageable.unpaged());
            for (Project otherProject : projects)
            {
                projectSubmissions.addSubmissions(otherProject.getSubmissionEnvelopes());
            }

            if (projectSubmissions.HasMissingSubmissions()) {
                missingProjectSubmissions.add(projectSubmissions);
            }
        }
        return new PageImpl<>(missingProjectSubmissions, pageable, missingProjectSubmissions.size());
    }
}
