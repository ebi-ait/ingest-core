package org.humancellatlas.ingest.export.job.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
public class ExportJobSearchProcessor implements ResourceProcessor<RepositorySearchesResource> {

    @Override
    public RepositorySearchesResource process(RepositorySearchesResource searchesResource) {
        if(searchesResource.getDomainType().equals(ExportJob.class)) {
            searchesResource.add(linkTo(methodOn(ExportJobController.class).findExportJobs(null, null, null, null, null, null))
                                     .withRel(Links.EXPORT_JOB_FIND_REL));
        }

        return searchesResource;
    }
}
