package org.humancellatlas.ingest.core.web;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentResource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class MetadataDocumentResourceAssembler extends ResourceAssemblerSupport<MetadataDocument, MetadataDocumentResource> {

    public MetadataDocumentResourceAssembler() {
        super(MetadataController.class, MetadataDocumentResource.class);
    }

    @Override
    public MetadataDocumentResource toResource(MetadataDocument entity) {
        MetadataDocumentResource resource = createResourceWithId(entity.getId(), entity);
        return resource;
    }
}
