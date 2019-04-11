package org.humancellatlas.ingest.process.web;

import org.humancellatlas.ingest.process.Process;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by rolando on 29/06/2018.
 */
@Component
public class ProcessSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {

    @Override
    public RepositorySearchesResource process(RepositorySearchesResource resource) {
        if(resource.getDomainType().equals(Process.class)) {
            resource.add(linkTo(methodOn(ProcessController.class).findProcesessByInputBundleUuid(null, null, null)).withRel("findByInputBundleUuid"));
        }

        return resource;
    }
}
