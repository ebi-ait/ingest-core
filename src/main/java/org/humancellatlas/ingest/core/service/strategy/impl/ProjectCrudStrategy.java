package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProjectCrudStrategy implements MetadataCrudStrategy<Project> {
    private final @NonNull
    ProjectService projectService;

    @Override
    public Project saveMetadataDocument(Project document) {
        return projectService.getProjectRepository().save(document);
    }

    @Override
    public Project findMetadataDocument(String id) {
        return projectService.getProjectRepository().findOne(id);
    }
}