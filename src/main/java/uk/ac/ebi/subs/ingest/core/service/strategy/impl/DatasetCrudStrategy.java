package uk.ac.ebi.subs.ingest.core.service.strategy.impl;

import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.service.strategy.MetadataCrudStrategy;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@AllArgsConstructor
public class DatasetCrudStrategy implements MetadataCrudStrategy<Dataset> {
  private final @NonNull DatasetRepository datasetRepository;

  @Override
  public Dataset saveMetadataDocument(final Dataset document) {
    return datasetRepository.save(document);
  }

  @Override
  public Dataset findMetadataDocument(final String id) {
    return datasetRepository
        .findById(id)
        .orElseThrow(
            () -> {
              throw new ResourceNotFoundException();
            });
  }

  @Override
  public Dataset findOriginalByUuid(final String uuid) {
    return null;
  }

  @Override
  public Stream<Dataset> findBySubmissionEnvelope(final SubmissionEnvelope submissionEnvelope) {
    return null;
  }

  @Override
  public Collection<Dataset> findAllBySubmissionEnvelope(
      final SubmissionEnvelope submissionEnvelope) {
    return null;
  }

  @Override
  public void removeLinksToDocument(final Dataset document) {}

  @Override
  public void deleteDocument(final Dataset document) {
    datasetRepository.delete(document);
  }
}
