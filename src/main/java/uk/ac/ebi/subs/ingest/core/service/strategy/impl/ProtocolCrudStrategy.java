package uk.ac.ebi.subs.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@AllArgsConstructor
public class ProtocolCrudStrategy implements MetadataCrudStrategy<Protocol> {
  private final @NonNull ProtocolRepository protocolRepository;
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull MessageRouter messageRouter;

  @Override
  public Protocol saveMetadataDocument(Protocol document) {
    return protocolRepository.save(document);
  }

  @Override
  public Protocol findMetadataDocument(String id) {
    return protocolRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Protocol findOriginalByUuid(String uuid) {
    return protocolRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Stream<Protocol> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return protocolRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<Protocol> findAllBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return protocolRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(Protocol document) {
    processRepository
        .findByProtocolsContains(document)
        .forEach(
            process -> {
              process.getProtocols().remove(document);
              processRepository.save(process);
            });
  }

  @Override
  public void deleteDocument(Protocol document) {
    removeLinksToDocument(document);
    protocolRepository.delete(document);
  }
}
