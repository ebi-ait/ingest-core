package org.humancellatlas.ingest.core.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.stereotype.Component;

@Component
public class SpringLinkGenerator implements LinkGenerator {

    @Autowired
    private ResourceMappings resourceMappings;

    @Override
    public String createCallback(Class<?> documentType, String documentId) {
        ResourceMetadata metadata = resourceMappings.getMetadataFor(documentType);
        return String.format("/%s/%s", metadata.getRel(), documentId);
    }

}
