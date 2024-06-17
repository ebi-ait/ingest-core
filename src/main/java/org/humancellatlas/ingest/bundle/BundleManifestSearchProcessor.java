package org.humancellatlas.ingest.bundle;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.humancellatlas.ingest.bundle.web.BundleManifestController;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
public class BundleManifestSearchProcessor
    implements ResourceProcessor<RepositorySearchesResource> {
  @Override
  public RepositorySearchesResource process(RepositorySearchesResource resource) {
    if (resource.getDomainType().equals(BundleManifest.class)) {
      resource.add(
          linkTo(
                  methodOn(BundleManifestController.class)
                      .findBundleManifestsByProjectUuidAndBundleType(null, null, null, null))
              .withRel("findBundleManifestsByProjectUuidAndBundleType"));
    }

    return resource;
  }
}
