package org.humancellatlas.ingest.project.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Project.class)
@RequiredArgsConstructor
@Getter
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private final @NonNull ProjectService projectService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    private final @NonNull ProjectRepository projectRepository;

    private final @NonNull JsonPatcher jsonPatcher;

    @PostMapping("/projects")
    ResponseEntity<Resource<?>> register(@RequestBody final Project project,
            final PersistentEntityResourceAssembler assembler) {
        Project result = projectService.register(project);
        return ResponseEntity.ok().body(assembler.toFullResource(result));
    }

    @PatchMapping("/projects/{id}")
    ResponseEntity<Resource<?>> update(@PathVariable("id") final Project project, @RequestBody final ObjectNode patch,
            final PersistentEntityResourceAssembler assembler) {
        jsonPatcher.merge(patch, project);
        projectRepository.save(project);
        return ResponseEntity.ok().body(assembler.toFullResource(project));
    }

    @PostMapping(path = "submissionEnvelopes/{sub_id}/projects")
    ResponseEntity<Resource<?>> addProjectToEnvelope(
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            @RequestBody Project project,
            @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
            PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            project.setUuid(new Uuid(uuid.toString()));
            project.setIsUpdate(true);
        });
        Project entity = getProjectService().addProjectToSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @GetMapping(path = "/projects/{id}/bundleManifests")
    ResponseEntity<PagedResources<Resource<BundleManifest>>> getBundleManifests(
            @PathVariable("id") Project project,
            @RequestParam("bundleType") Optional<BundleType> bundleType,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = projectService.getBundleManifestRepository().findBundleManifestsByProjectAndBundleType(project, bundleType.orElse(null), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    @GetMapping(path = "/projects/{id}/submissionEnvelopes")
    ResponseEntity<PagedResources<Resource<SubmissionEnvelope>>> getProjectSubmissionEnvelopes(
            @PathVariable("id") Project project, Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        var envelopes = projectService.getSubmissionEnvelopes(project);
        var resultPage = new PageImpl<>(new ArrayList<>(envelopes), pageable, envelopes.size());
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(resultPage, resourceAssembler));
    }

    @PostMapping(path = "/projects/query")
    ResponseEntity<PagedResources<Resource<Project>>> queryProjects(
            @RequestBody List<MetadataCriteria> criteriaList,
            @RequestParam("operator") Optional<String> operator,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Boolean andCriteria = false;
        if (operator.isPresent() && operator.get().toLowerCase().equals("and"))
            andCriteria = true; //otherwise "or" will be used.
        Page<Project> projects = projectService.findByCriteria(criteriaList, andCriteria, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(projects, resourceAssembler));
    }

    @PutMapping(path = "projects/{proj_id}/submissionEnvelopes/{sub_id}")
    ResponseEntity<Resource<?>> linkSubmissionToProject (
            @PathVariable("proj_id") Project project,
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            PersistentEntityResourceAssembler assembler) {
        Project savedProject = getProjectService().linkProjectSubmissionEnvelope(submissionEnvelope, project);
        PersistentEntityResource projectResource = assembler.toFullResource(savedProject);
        return ResponseEntity.accepted().body(projectResource);
    }

    @DeleteMapping(path = "projects/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Project project) {
        try {
            projectService.delete(project);
            return ResponseEntity.noContent().build();
        } catch (NonEmptyProject nonEmptyProject) {
            String message = nonEmptyProject.getMessage();
            LOGGER.debug(message);
            Map<String, String> errorResponse = Map.of("message", message);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

}