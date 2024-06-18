package org.humancellatlas.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@Component
@AllArgsConstructor
public class ProjectCrudStrategy implements MetadataCrudStrategy<Project> {
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull ProtocolRepository protocolRepository;
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull FileRepository fileRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull MessageRouter messageRouter;

  @Override
  public Project saveMetadataDocument(Project document) {
    return projectRepository.save(document);
  }

  @Override
  public Project findMetadataDocument(String id) {
    return projectRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Project findOriginalByUuid(String uuid) {
    return projectRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Stream<Project> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return projectRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<Project> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return projectRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(Project document) {
    messageRouter.routeStateTrackingDeleteMessageFor(document);
    biomaterialRepository
        .findByProject(document)
        .forEach(
            biomaterial -> {
              biomaterial.setProject(null);
              biomaterialRepository.save(biomaterial);
            });
    biomaterialRepository
        .findByProjectsContaining(document)
        .forEach(
            biomaterial -> {
              biomaterial.getProjects().remove(document);
              biomaterialRepository.save(biomaterial);
            });
    fileRepository
        .findByProject(document)
        .forEach(
            file -> {
              file.setProject(null);
              fileRepository.save(file);
            });
    processRepository
        .findByProject(document)
        .forEach(
            process -> {
              process.setProject(null);
              processRepository.save(process);
            });
    processRepository
        .findByProjectsContaining(document)
        .forEach(
            process -> {
              process.getProjects().remove(document);
              processRepository.save(process);
            });
    protocolRepository
        .findByProject(document)
        .forEach(
            protocol -> {
              protocol.setProject(null);
              protocolRepository.save(protocol);
            });
  }

  @Override
  public void deleteDocument(Project document) {
    removeLinksToDocument(document);
    projectRepository.delete(document);
  }
}
