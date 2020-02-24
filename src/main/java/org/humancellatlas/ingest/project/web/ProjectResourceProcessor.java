package org.humancellatlas.ingest.project.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.project.Project;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResourceProcessor implements ResourceProcessor<Resource<Project>> {
    private final @NonNull EntityLinks entityLinks;

    @Override
    public Resource<Project> process(Resource<Project> resource) {
        Project project = resource.getContent();
        resource.add(entityLinks.linkForSingleResource(project)
                .slash("bundleManifests")
                .withRel("bundleManifests"));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash("biomaterials")
                .withRel("biomaterials"));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash("processes")
                .withRel("processes"));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash("files")
                .withRel("files"));
        resource.add(entityLinks.linkForSingleResource(project)
                .slash("protocols")
                .withRel("protocols"));
        return resource;
    }

}
