package org.humancellatlas.ingest.export.job.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExportJobResourceProcessor implements ResourceProcessor<Resource<ExportJob>> {
    private final @NonNull EntityLinks entityLinks;

    private Link getEntitiesLink(ExportJob exportJob) {
        return entityLinks.linkForSingleResource(exportJob)
            .slash("/entities")
            .withRel("entities");
    }

    @Override
    public Resource<ExportJob> process(Resource<ExportJob> resource) {
        ExportJob exportJob = resource.getContent();
        resource.add(getEntitiesLink(exportJob));
        return resource;
    }
}
