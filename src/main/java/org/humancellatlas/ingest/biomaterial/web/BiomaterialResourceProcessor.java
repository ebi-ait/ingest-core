package org.humancellatlas.ingest.biomaterial.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.web.Links;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BiomaterialResourceProcessor implements ResourceProcessor<Resource<Biomaterial>>  {
    private final @NonNull EntityLinks entityLinks;

    private Link getInputToProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.INPUT_TO_PROCESSES_URL)
                .withRel(Links.INPUT_TO_PROCESSES_REL);
    }

    private Link getDerivedByProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.DERIVED_BY_PROCESSES_URL)
                .withRel(Links.DERIVED_BY_PROCESSES_REL);
    }

    @Override
    public Resource<Biomaterial> process(Resource<Biomaterial> resource) {
        Biomaterial biomaterial = resource.getContent();
        resource.add(getInputToProcessesLink(biomaterial));
        resource.add(getDerivedByProcessesLink(biomaterial));
        return resource;
    }
}
