package org.humancellatlas.ingest.core.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.support.RepositoryLinkBuilder;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SpringLinkGenerator implements LinkGenerator {

    private static final String DUMMY_BASE_URI = "http://localhost:8080";

    @Autowired
    private ResourceMappings resourceMappings;

    //TODO verify that this works (ideally with some unit tests)
    //It's quite difficult to write tests for this as the dependency is quite complicated to fake
    /**
    This was previously defined in
     {@link org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder},
     which is assumed to be working.
    */
    @Override
    public String createCallback(Class<?> documentType, String documentId) {
        /* todo - here, we make link with DUMMY_BASE_URI and then take it out again so clients
           can fill in domain - must be a better way of doing this! */
         ResourceMetadata metadata = resourceMappings.getMetadataFor(documentType);
        RepositoryLinkBuilder linkBuilder = new RepositoryLinkBuilder(metadata,
                new BaseUri(URI.create(DUMMY_BASE_URI)));
        Link link = linkBuilder.slash(documentId).withRel(metadata.getItemResourceRel());
        return link.withSelfRel().getHref().replace(DUMMY_BASE_URI, "");
    }

}
