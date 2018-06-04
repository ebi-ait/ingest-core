package org.humancellatlas.ingest.schemas.web;


import org.humancellatlas.ingest.schemas.Schema;

import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;


/**
 * Created by rolando on 23/04/2018.
 */
@Component
public class SchemaSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {

    @Override
    public RepositorySearchesResource process(RepositorySearchesResource searchesResource) {
        if(searchesResource.getDomainType().equals(Schema.class)) {
            searchesResource.add(linkTo(methodOn(SchemaController.class).latestSchemas(null, null)).withRel("latestSchemas"));
            searchesResource.add(linkTo(methodOn(SchemaController.class).filterLatestSchemas(null, null, null)).withRel("filterLatestSchemas"));
        }

        return searchesResource;
    }
}
