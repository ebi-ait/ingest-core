package org.humancellatlas.ingest.bundle.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleType;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(BundleManifest.class)
@Getter
public class BundleManifestController {
    private final @NonNull
    ProjectService projectService;

    private final @NonNull
    PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/projects/search/findBundleManifestsByProjectUuidAndBundleType", method = RequestMethod.GET)
    public ResponseEntity<?> findBundleManifestsByProjectUuidAndBundleType(@RequestParam("projectUuid") Uuid projectUuid,
                                                  @RequestParam("bundleType") Optional<BundleType> bundleType,
                                                  Pageable pageable,
                                                  final PersistentEntityResourceAssembler resourceAssembler) {

        Page<BundleManifest> bundleManifests = this.projectService.findBundleManifestsByProjectUuidAndBundleType(projectUuid, bundleType.orElse(null), pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(bundleManifests, resourceAssembler));
    }
}
