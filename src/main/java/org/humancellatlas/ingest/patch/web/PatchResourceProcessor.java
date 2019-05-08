package org.humancellatlas.ingest.patch.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.patch.Patch;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatchResourceProcessor implements ResourceProcessor<Resource<Patch<?>>> {
    private final @NonNull EntityLinks entityLinks;


    @Override
    public Resource<Patch<?>> process(Resource<Patch<?>> resource) {
        Link originalDocumentLink = entityLinks.linkForSingleResource(resource.getContent().getOriginalDocument())
                                               .withRel("originalDocument");

        Link updateDocumentLink = entityLinks.linkForSingleResource(resource.getContent().getUpdateDocument())
                                               .withRel("updateDocument");

        resource.add(originalDocumentLink);
        resource.add(updateDocumentLink);

        return resource;
    }
}
