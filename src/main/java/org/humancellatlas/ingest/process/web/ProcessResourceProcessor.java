package org.humancellatlas.ingest.process.web;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.humancellatlas.ingest.process.Process;

@Component
@RequiredArgsConstructor
public class ProcessResourceProcessor implements ResourceProcessor<Resource<Process>> {
    private final @NonNull
    EntityLinks entityLinks;

    private Link getInputBiomaterialsLink(Process process) {
        return entityLinks.linkForSingleResource(process)
                          .slash(Links.INPUT_BIOMATERIALS_URL)
                          .withRel(Links.INPUT_BIOMATERIALS_REL);
    }

    private Link getDerivedBiomaterialsLink(Process process) {
        return entityLinks.linkForSingleResource(process)
                          .slash(Links.DERIVED_BY_BIOMATERIALS_URL)
                          .withRel(Links.DERIVED_BY_BIOMATERIALS_REL);
    }

    private Link getInputFilesLink(Process process) {
        return entityLinks.linkForSingleResource(process)
                          .slash(Links.INPUT_FILES_URL)
                          .withRel(Links.INPUT_FILES_REL);
    }

    private Link getDerivedFilesLink(Process process) {
        return entityLinks.linkForSingleResource(process)
                          .slash(Links.DERIVED_BY_FILES_URL)
                          .withRel(Links.DERIVED_BY_FILES_REL);
    }

    @Override
    public Resource<Process> process(Resource<Process> resource) {
        Process process = resource.getContent();
        resource.add(getInputBiomaterialsLink(process));
        resource.add(getDerivedBiomaterialsLink(process));
        resource.add(getInputFilesLink(process));
        resource.add(getDerivedFilesLink(process));
        return resource;
    }
}