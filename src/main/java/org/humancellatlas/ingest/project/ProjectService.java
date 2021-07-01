package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.notifications.model.Notification;
import org.humancellatlas.ingest.notifications.processors.impl.email.EmailMetadata;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;


@Service
@RequiredArgsConstructor
@Getter
public class ProjectService {

    //Helper class for capturing copies of a Project and all Submission Envelopes related to them.
    private static class ProjectBag {

        private final Set<Project> projects;
        private final Set<SubmissionEnvelope> submissionEnvelopes;

        public ProjectBag(Set<Project> projects, Set<SubmissionEnvelope> submissionEnvelopes) {
            this.projects = projects;
            this.submissionEnvelopes = submissionEnvelopes;
        }

    }

    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProjectRepository projectRepository;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull BundleManifestRepository bundleManifestRepository;

    private final @NonNull ProjectEventHandler projectEventHandler;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Project register(final Project project) {
        project.setCataloguedDate(null);
        if (!isNull(project.getIsInCatalogue()) && project.getIsInCatalogue()) {
            project.setCataloguedDate(Instant.now());
        }
        Project persistentProject = projectRepository.save(project);
        projectEventHandler.registeredProject(persistentProject);
        return persistentProject;
    }

    public Project createSuggestedProject(final ObjectNode suggestion) {
        Project suggestedProject = new Project(null);
        suggestedProject.setWranglingState(WranglingState.NEW_SUGGESTION);
        var notes = String.format(
            "DOI: %s \nName: %s \nEmail: %s \nComments: %s",
            suggestion.get("doi"),
            suggestion.get("name"),
            suggestion.get("email"),
            suggestion.get("comments")
        );
        suggestedProject.setWranglingNotes(notes);
        Project persistentSuggestion = this.register(suggestedProject);
        this.notifyWranglersOfNewSuggestion(persistentSuggestion);
        return persistentSuggestion;
    }

    private void notifyWranglersOfNewSuggestion(Project suggestion) {
        // Build Email Metadata
        // Real values needed
        var email = new EmailMetadata();
        email.setFrom("some@email.com");
        email.setTo("another@email.com");
        email.setSubject("New Project Suggestion");
        email.setBody(suggestion.getUuid().getUuid().toString());
        // How to get EmailMetadata handled EmailNotificationProcessor?

        // The Could build a json map and send to the notifications prococessor?
        // Map<String, ?> notification = new Map<String, Object>()
        // var notification = Notification.buildNew().metadata({"email": {}});
 }

    public Project update(final Project project, ObjectNode patch, Boolean sendNotification) {
        if (patch.has("isInCatalogue")
            && patch.get("isInCatalogue").asBoolean()
            && project.getCataloguedDate() == null){
            project.setCataloguedDate(Instant.now());
        }

        Project updatedProject = metadataUpdateService.update(project, patch);

        if (sendNotification) {
            projectEventHandler.editedProjectMetadata(updatedProject);
        }
        return  updatedProject;
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

    public Page<BundleManifest> findBundleManifestsByProjectUuidAndBundleType(Uuid projectUuid, BundleType bundleType,
            Pageable pageable) {
        return this.projectRepository
                .findByUuidUuidAndIsUpdateFalse(projectUuid.getUuid())
                .map(project -> bundleManifestRepository.findBundleManifestsByProjectAndBundleType(project,
                        bundleType, pageable))
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException(format("Project with UUID %s not found",
                            projectUuid.getUuid().toString()));
                });
    }

    public Set<SubmissionEnvelope> getSubmissionEnvelopes(Project project) {
        return gather(project).submissionEnvelopes;
    }

    public void delete(Project project) throws NonEmptyProject {
        ProjectBag projectBag = gather(project);
        if (projectBag.submissionEnvelopes.isEmpty()) {
            projectBag.projects.forEach(_project -> {
                projectRepository.delete(_project);
                projectEventHandler.deletedProject(_project);
            });
        } else {
            throw new NonEmptyProject();
        }
    }

    private ProjectBag gather(Project project) {
        Set<SubmissionEnvelope> envelopes = new HashSet<>();
        Set<Project> projects = this.projectRepository.findByUuid(project.getUuid()).collect(toSet());
        projects.forEach(copy -> {
            envelopes.addAll(copy.getSubmissionEnvelopes());
            envelopes.add(copy.getSubmissionEnvelope());
        });

        //ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
        envelopes.removeIf(env -> env == null || env.getSubmissionState() == null);
        return new ProjectBag(projects, envelopes);
    }

}
