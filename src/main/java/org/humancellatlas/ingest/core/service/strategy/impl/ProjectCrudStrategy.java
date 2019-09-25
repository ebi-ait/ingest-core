package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProjectCrudStrategy implements MetadataCrudStrategy<Project> {
    private final @NonNull
    ProjectRepository projectRepository;

    @Override
    public Project saveMetadataDocument(Project document) {
        return projectRepository.save(document);
    }

    @Override
    public Project findMetadataDocument(String id) {
        return projectRepository.findById(id)
                                .orElseThrow(() -> {
                                    throw new ResourceNotFoundException();
                                });
    }

    @Override
    public Project findOriginalByUuid(String uuid) {
        return projectRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
                                .orElseThrow(() -> {
                                    throw new ResourceNotFoundException();
                                });
    }

    @Override
    public Stream<Project> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return projectRepository.findBySubmissionEnvelope(submissionEnvelope);
    }
}