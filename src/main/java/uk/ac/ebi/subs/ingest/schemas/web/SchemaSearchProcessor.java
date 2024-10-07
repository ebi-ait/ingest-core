package uk.ac.ebi.subs.ingest.schemas.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import uk.ac.ebi.subs.ingest.schemas.Schema;

/** Created by rolando on 23/04/2018. */
@Component
public class SchemaSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {

  @Override
  public RepositorySearchesResource process(RepositorySearchesResource searchesResource) {
    if (searchesResource.getDomainType().equals(Schema.class)) {
      searchesResource.add(
          linkTo(methodOn(SchemaController.class).latestSchemas(null, null))
              .withRel("latestSchemas"));
      searchesResource.add(
          linkTo(methodOn(SchemaController.class).filterLatestSchemas(null, null, null))
              .withRel("filterLatestSchemas"));
    }

    return searchesResource;
  }
}
