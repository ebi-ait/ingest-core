package uk.ac.ebi.subs.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@AllArgsConstructor
public class ProcessCrudStrategy implements MetadataCrudStrategy<Process> {
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull FileRepository fileRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull MessageRouter messageRouter;

  @Override
  public Process saveMetadataDocument(Process document) {
    return processRepository.save(document);
  }

  @Override
  public Process findMetadataDocument(String id) {
    return processRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Process findOriginalByUuid(String uuid) {
    return processRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Stream<Process> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return processRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<Process> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return processRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(Process document) {
    fileRepository
        .findByInputToProcessesContains(document)
        .forEach(
            file -> {
              file.getInputToProcesses().remove(document);
              fileRepository.save(file);
            });
    fileRepository
        .findByDerivedByProcessesContains(document)
        .forEach(
            file -> {
              file.getDerivedByProcesses().remove(document);
              fileRepository.save(file);
            });
    biomaterialRepository
        .findByInputToProcessesContains(document)
        .forEach(
            biomaterial -> {
              biomaterial.getInputToProcesses().remove(document);
              biomaterialRepository.save(biomaterial);
            });
    biomaterialRepository
        .findByDerivedByProcessesContains(document)
        .forEach(
            biomaterial -> {
              biomaterial.getDerivedByProcesses().remove(document);
              biomaterialRepository.save(biomaterial);
            });
  }

  @Override
  public void deleteDocument(Process document) {
    removeLinksToDocument(document);
    processRepository.delete(document);
  }
}
