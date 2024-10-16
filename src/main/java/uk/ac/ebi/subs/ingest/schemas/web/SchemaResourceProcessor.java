package uk.ac.ebi.subs.ingest.schemas.web;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import uk.ac.ebi.subs.ingest.schemas.Schema;

/** Created by rolando on 19/04/2018. */
@Component
public class SchemaResourceProcessor implements ResourceProcessor<Resource<Schema>> {

  public Resource<Schema> process(Resource<Schema> resource) {
    resource.add(new Link(resource.getContent().getSchemaUri(), "json-schema"));
    return resource;
  }
}
