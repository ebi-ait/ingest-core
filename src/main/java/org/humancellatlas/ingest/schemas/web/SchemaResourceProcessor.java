package org.humancellatlas.ingest.schemas.web;

import org.humancellatlas.ingest.schemas.Schema;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * Created by rolando on 19/04/2018.
 */
@Component
public class SchemaResourceProcessor implements ResourceProcessor<Resource<Schema>> {

    public Resource<Schema> process(Resource<Schema> resource) {
        resource.add(new Link(resource.getContent().getSchemaUri(), "json-schema"));
        return resource;
    }
}
