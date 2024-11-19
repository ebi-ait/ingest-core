package uk.ac.ebi.subs.ingest.bundle;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

import uk.ac.ebi.subs.ingest.bundle.web.BundleManifestController;

@Component
public class BundleManifestSearchProcessor
    implements ResourceProcessor<RepositorySearchesResource> {
  @Override
  public RepositorySearchesResource process(RepositorySearchesResource resource) {
    if (resource.getDomainType().equals(BundleManifest.class)) {
      resource.add(
          ControllerLinkBuilder.linkTo(
                  methodOn(BundleManifestController.class)
                      .findBundleManifestsByProjectUuidAndBundleType(null, null, null, null))
              .withRel("findBundleManifestsByProjectUuidAndBundleType"));
    }

    return resource;
  }
}
