package org.humancellatlas.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Component
public class BiomaterialCrudStrategy implements MetadataCrudStrategy<Biomaterial> {
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull MessageRouter messageRouter;

  @Override
  public Biomaterial saveMetadataDocument(Biomaterial document) {
    return biomaterialRepository.save(document);
  }

  @Override
  public Biomaterial findMetadataDocument(String id) {
    return biomaterialRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Biomaterial findOriginalByUuid(String uuid) {
    return biomaterialRepository
        .findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Stream<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
    return biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public Collection<Biomaterial> findAllBySubmissionEnvelope(
      SubmissionEnvelope submissionEnvelope) {
    return biomaterialRepository.findAllBySubmissionEnvelope(submissionEnvelope);
  }

  @Override
  public void removeLinksToDocument(Biomaterial document) {
    // TODO: handle locally
    // messageRouter.routeStateTrackingDeleteMessageFor(document);
  }

  @Override
  public void deleteDocument(Biomaterial document) {
    removeLinksToDocument(document);
    biomaterialRepository.delete(document);
  }
}
