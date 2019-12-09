package org.humancellatlas.ingest.project.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final @NonNull ProjectService projectService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

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
            @PathVariable("id") Project project,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Page<SubmissionEnvelope> envelopes = projectService.getProjectSubmissionEnvelopes(project, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(envelopes, resourceAssembler));
    }

    @PostMapping(path = "/projects/query")
    ResponseEntity<PagedResources<Resource<Project>>> queryProjects(
            @RequestBody List<MetadataCriteria> query,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Page<Project> projects = projectService.queryByContent(query, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(projects, resourceAssembler));
    }
}