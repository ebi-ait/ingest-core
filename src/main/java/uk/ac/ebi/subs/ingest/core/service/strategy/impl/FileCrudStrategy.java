package uk.ac.ebi.subs.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@AllArgsConstructor
public class FileCrudStrategy implements MetadataCrudStrategy<File> {
  private final @NonNull FileRepository fileRepository;
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull MessageRouter messageRouter;

  @Override
  public File saveMetadataDocument(File document) {
    return fileRepository.save(document);
  }

  @Override
  public File findMetadataDocument(String id) {
    return fileRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public File findOriginalByUuid(String uuid) {
    return fileRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Stream<File> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return fileRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<File> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return fileRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(File document) {
    projectRepository
        .findBySupplementaryFilesContains(document)
        .forEach(
            project -> {
              project.getSupplementaryFiles().remove(document);
              projectRepository.save(project);
            });
  }

  @Override
  public void deleteDocument(File document) {
    removeLinksToDocument(document);
    fileRepository.delete(document);
  }
}
