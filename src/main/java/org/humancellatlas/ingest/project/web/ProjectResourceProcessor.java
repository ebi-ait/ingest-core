package org.humancellatlas.ingest.project.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.project.Project;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResourceProcessor implements ResourceProcessor<Resource<Project>>{
    private final @NonNull EntityLinks entityLinks;

    @Override
    public Resource<Project> process(Resource<Project> resource) {
        Project project = resource.getContent();
        resource.add(entityLinks.linkForSingleResource(project)
                .slash(Links.PRIMARY_BUNDLES_URL)
                .withRel(Links.PRIMARY_BUNDLES_REL));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash(Links.ANALYSIS_BUNDLES_URL)
                .withRel(Links.ANALYSIS_BUNDLES_REL));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash(Links.ALL_BUNDLES_URL)
                .withRel(Links.ALL_BUNDLES_REL));

        return resource;
    }

}
