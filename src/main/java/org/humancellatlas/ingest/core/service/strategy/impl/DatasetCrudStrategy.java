package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

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
        return datasetRepository.findById(id)
                .orElseThrow(() -> {
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
    public Collection<Dataset> findAllBySubmissionEnvelope(final SubmissionEnvelope submissionEnvelope) {
        return null;
    }

    @Override
    public void removeLinksToDocument(final Dataset document) {

    }

    @Override
    public void deleteDocument(final Dataset document) {
        datasetRepository.delete(document);
    }
}
