package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
        return projectRepository.findOne(id);
    }

    @Override
    public Project findOriginalByUuid(String uuid) {
        return projectRepository.findByUuidAndIsUpdateFalse(new Uuid(uuid));
    }

    @Override
    public Collection<Project> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return projectRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
    }
}