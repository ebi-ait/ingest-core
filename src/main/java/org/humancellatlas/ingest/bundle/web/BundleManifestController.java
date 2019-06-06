package org.humancellatlas.ingest.bundle.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(BundleManifest.class)
@Getter
public class BundleManifestController {
    private final @NonNull
    ProjectService projectService;

    private final @NonNull
    PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/projects/search/findPrimaryBundles", method = RequestMethod.GET)
    public ResponseEntity<?> findPrimaryBundlesByProjectUuid(@RequestParam("projectUuid") Uuid projectUuid,
                                                               Pageable pageable,
                                                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = this.findBundles(projectUuid, Boolean.TRUE, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    @RequestMapping(path = "/projects/search/findAnalysisBundles", method = RequestMethod.GET)
    public ResponseEntity<?> findAnalysisBundlesByProjectUuid(@RequestParam("projectUuid") Uuid projectUuid,
                                                               Pageable pageable,
                                                               final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = this.findBundles(projectUuid, Boolean.FALSE, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    @RequestMapping(path = "/projects/search/findAllBundles", method = RequestMethod.GET)
    public ResponseEntity<?> findAllByProjectUuid(@RequestParam("projectUuid") Uuid projectUuid,
                                                              Pageable pageable,
                                                              final PersistentEntityResourceAssembler resourceAssembler) {
        Page<BundleManifest> bundleManifests = this.findBundles(projectUuid, Boolean.FALSE, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }

    private Page<BundleManifest> findBundles(Uuid projectUuid, Boolean isPrimary, Pageable pageable) {
        Project project = this.projectService.getProjectRepository().findByUuidAndIsUpdateFalse(projectUuid);
        if (project == null) {
            throw new ResourceNotFoundException(String.format("Project with UUID %s not found", projectUuid.getUuid().toString()));
        }
        if (isPrimary == null ) return projectService.findBundlesByProject(project, null, pageable);
        return projectService.findBundlesByProject(project, isPrimary, pageable);
    }
}
