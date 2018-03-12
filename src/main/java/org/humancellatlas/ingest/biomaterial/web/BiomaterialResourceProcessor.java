package org.humancellatlas.ingest.biomaterial.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BiomaterialResourceProcessor implements ResourceProcessor<Resource<Biomaterial>>  {

    private final @NonNull EntityLinks entityLinks;

    private Link getBiomaterialsInputToProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.BIOMATERIALS_INPUT_TO_PROCESSES_URL)
                .withRel(Links.BIOMATERIALS_INPUT_TO_PROCESSES_REL);
    }

    private Link getBiomaterialsDerivedByProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.BIOMATERIALS_DERIVED_BY_PROCESSES_URL)
                .withRel(Links.BIOMATERIALS_DERIVED_BY_PROCESSES_REL);
    }

    private Link getFilesInputToProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.FILES_INPUT_TO_PROCESSES_URL)
                .withRel(Links.FILES_INPUT_TO_PROCESSES_REL);
    }

    private Link getFilesDerivedByProcessesLink(Biomaterial biomaterial) {
        return entityLinks.linkForSingleResource(biomaterial)
                .slash(Links.FILES_DERIVED_BY_PROCESSES_URL)
                .withRel(Links.FILES_DERIVED_BY_PROCESSES_REL);
    }

    @Override
    public Resource<Biomaterial> process(Resource<Biomaterial> resource) {
        Biomaterial biomaterial = resource.getContent();
        resource.add(getBiomaterialsInputToProcessesLink(biomaterial));
        resource.add(getBiomaterialsDerivedByProcessesLink(biomaterial));
        resource.add(getFilesInputToProcessesLink(biomaterial));
        resource.add(getFilesDerivedByProcessesLink(biomaterial));
        return resource;
    }

}
