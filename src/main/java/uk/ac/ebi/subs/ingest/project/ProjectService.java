package uk.ac.ebi.subs.ingest.project;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.util.*;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.audit.AuditEntry;
import uk.ac.ebi.subs.ingest.audit.AuditEntryService;
import uk.ac.ebi.subs.ingest.audit.AuditType;
import uk.ac.ebi.subs.ingest.bundle.BundleManifest;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.bundle.BundleType;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;
import uk.ac.ebi.subs.ingest.project.exception.NonEmptyProject;
import uk.ac.ebi.subs.ingest.project.web.SearchFilter;
import uk.ac.ebi.subs.ingest.schemas.SchemaService;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@Service
@RequiredArgsConstructor
@Getter
public class ProjectService {
  @Autowired private final MongoTemplate mongoTemplate;

  // Helper class for capturing copies of a Project and all Submission Envelopes related to them.
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
  private final @NotNull DatasetRepository datasetRepository;
  private final @NonNull MetadataCrudService metadataCrudService;
  private final @NonNull MetadataUpdateService metadataUpdateService;
  private final @NonNull SchemaService schemaService;
  private final @NonNull BundleManifestRepository bundleManifestRepository;
  private final @NonNull AuditEntryService auditEntryService;

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
    Map<String, String> content = createBaseContentForProject();
    Project suggestedProject = new Project(content);
    suggestedProject.setWranglingState(WranglingState.NEW_SUGGESTION);
    var notes =
        String.format(
            "DOI: %s \nName: %s \nEmail: %s \nComments: %s",
            suggestion.get("doi"),
            suggestion.get("name"),
            suggestion.get("email"),
            suggestion.get("comments"));
    suggestedProject.setWranglingNotes(notes);
    return this.register(suggestedProject);
  }

  public Project update(final Project project, ObjectNode patch, Boolean sendNotification) {
    if (patch.has("isInCatalogue")
        && patch.get("isInCatalogue").asBoolean()
        && project.getCataloguedDate() == null) {
      project.setCataloguedDate(Instant.now());
    }

    updateWranglingState(project, patch);
    Project updatedProject = metadataUpdateService.update(project, patch);

    if (sendNotification) {
      projectEventHandler.editedProjectMetadata(updatedProject);
    }

    return updatedProject;
  }

  private void updateWranglingState(Project project, ObjectNode patch) {
    Optional.ofNullable(patch.get("wranglingState"))
        .map(JsonNode::asText)
        .map(WranglingState::getName)
        .ifPresent(newWranglingState -> updateWranglingState(project, newWranglingState));
  }

  public void updateWranglingState(Project project, @NonNull WranglingState newWranglingState) {
    WranglingState currentWranglingState = project.getWranglingState();
    if (currentWranglingState != newWranglingState) {
      log.info(
          "setting project {} from {} to {}",
          project.getId(),
          currentWranglingState,
          newWranglingState);
      project.setWranglingState(newWranglingState);
      projectRepository.save(project);
      AuditEntry wranglingStateUpdate =
          new AuditEntry(
              AuditType.STATUS_UPDATED, currentWranglingState, newWranglingState, project);
      auditEntryService.addAuditEntry(wranglingStateUpdate);
    }
  }

  public Project addProjectToSubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Project project) {
    if (!project.getIsUpdate()) {
      return metadataCrudService.addToSubmissionEnvelopeAndSave(project, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(project, submissionEnvelope);
    }
  }

  public Project linkProjectSubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope, Project project) {
    final String projectId = project.getId();
    project.addToSubmissionEnvelopes(submissionEnvelope);
    projectRepository.save(project);

    projectRepository
        .findByUuidUuidAndIsUpdateFalse(project.getUuid().getUuid())
        .ifPresent(
            projectByUuid -> {
              if (!projectByUuid.getId().equals(projectId)) {
                projectByUuid.addToSubmissionEnvelopes(submissionEnvelope);
                projectRepository.save(projectByUuid);
              }
            });
    return project;
  }

  public Page<BundleManifest> findBundleManifestsByProjectUuidAndBundleType(
      Uuid projectUuid, BundleType bundleType, Pageable pageable) {
    return this.projectRepository
        .findByUuidUuidAndIsUpdateFalse(projectUuid.getUuid())
        .map(
            project ->
                bundleManifestRepository.findBundleManifestsByProjectAndBundleType(
                    project, bundleType, pageable))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException(
                  format("Project with UUID %s not found", projectUuid.getUuid().toString()));
            });
  }

  public Set<SubmissionEnvelope> getSubmissionEnvelopes(Project project) {
    return gather(project).submissionEnvelopes;
  }

  public void delete(Project project) throws NonEmptyProject {
    ProjectBag projectBag = gather(project);
    if (projectBag.submissionEnvelopes.isEmpty()) {
      projectBag.projects.forEach(
          _project -> {
            metadataCrudService.deleteDocument(_project);
            projectEventHandler.deletedProject(_project);
          });
    } else {
      throw new NonEmptyProject();
    }
  }

  private Map<String, String> createBaseContentForProject() {
    Map<String, String> content = new HashMap<>();
    final String entityType = "project";
    final String highLevelEntity = "type";
    content.put(
        "describedBy",
        schemaService.getLatestSchemaByEntityType(highLevelEntity, entityType).getSchemaUri());
    content.put("schema_type", entityType);
    return content;
  }

  private ProjectBag gather(Project project) {
    Set<SubmissionEnvelope> envelopes = new HashSet<>();
    Set<Project> projects = this.projectRepository.findByUuid(project.getUuid()).collect(toSet());
    projects.forEach(
        copy -> {
          envelopes.addAll(copy.getSubmissionEnvelopes());
          envelopes.add(copy.getSubmissionEnvelope());
        });

    // ToDo: Find a better way of ensuring that DBRefs to deleted objects aren't returned.
    envelopes.removeIf(env -> env == null || env.getSubmissionState() == null);
    return new ProjectBag(projects, envelopes);
  }

  public Page<Project> filterProjects(SearchFilter searchFilter, Pageable pageable) {
    Query query = ProjectQueryBuilder.buildProjectsQuery(searchFilter);
    log.debug("Project Search query: " + query);

    List<Project> projects = mongoTemplate.find(query.with(pageable), Project.class);
    long count = mongoTemplate.count(query, Project.class);
    return new PageImpl<>(projects, pageable, count);
  }

  public List<AuditEntry> getProjectAuditEntries(Project project) {
    return auditEntryService.getAuditEntriesForAbstractEntity(project);
  }

  public final Project linkDatasetToProject(final Project project, final Dataset dataset) {
    final String projectId = project.getId();
    final String datasetId = dataset.getId();

    projectRepository
        .findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project: " + projectId));
    datasetRepository
        .findById(datasetId)
        .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));

    project.addDataset(dataset);

    return projectRepository.save(project);
  }
}
