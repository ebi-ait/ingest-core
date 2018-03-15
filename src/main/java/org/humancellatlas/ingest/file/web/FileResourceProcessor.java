package org.humancellatlas.ingest.file.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author tburdett
 * @date 13/03/2018
 */
@Component
@RequiredArgsConstructor
public class FileResourceProcessor implements ResourceProcessor<Resource<File>> {
    private final @NonNull EntityLinks entityLinks;

    private Link getInputToProcessesLink(File file) {
        return entityLinks.linkForSingleResource(file)
                .slash(Links.INPUT_TO_PROCESSES_URL)
                .withRel(Links.INPUT_TO_PROCESSES_REL);
    }

    private Link getDerivedByProcessesLink(File file) {
        return entityLinks.linkForSingleResource(file)
                .slash(Links.DERIVED_BY_PROCESSES_URL)
                .withRel(Links.DERIVED_BY_PROCESSES_REL);
    }

    @Override
    public Resource<File> process(Resource<File> resource) {
        File file = resource.getContent();
        resource.add(getInputToProcessesLink(file));
        resource.add(getDerivedByProcessesLink(file));
        return resource;
    }
}
