package org.humancellatlas.ingest.bundle;

import org.humancellatlas.ingest.bundle.web.BundleManifestController;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class BundleManifestSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {
    @Override
    public RepositorySearchesResource process(RepositorySearchesResource resource) {
        if(resource.getDomainType().equals(BundleManifest.class)) {
            resource.add(linkTo(methodOn(BundleManifestController.class).findPrimaryBundlesByProjectUuid(null, null, null)).withRel("findPrimaryBundlesByProjectUuid"));
            resource.add(linkTo(methodOn(BundleManifestController.class).findAnalysisBundlesByProjectUuid(null, null, null)).withRel("findAnalysisBundlesByProjectUuid"));
            resource.add(linkTo(methodOn(BundleManifestController.class).findAllByProjectUuid(null, null, null)).withRel("findAllBundlesByProjectUuid"));
        }

        return resource;
    }
}