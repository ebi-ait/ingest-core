package uk.ac.ebi.subs.ingest.process.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import uk.ac.ebi.subs.ingest.process.Process;

/** Created by rolando on 29/06/2018. */
@Component
public class ProcessSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {

  @Override
  public RepositorySearchesResource process(RepositorySearchesResource resource) {
    if (resource.getDomainType().equals(Process.class)) {
      resource.add(
          linkTo(methodOn(ProcessController.class).findProcesessByInputBundleUuid(null, null, null))
              .withRel("findByInputBundleUuid"));
    }

    return resource;
  }
}
