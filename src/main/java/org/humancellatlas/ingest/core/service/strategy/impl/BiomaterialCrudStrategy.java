package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;


@AllArgsConstructor
@Component
public class BiomaterialCrudStrategy implements MetadataCrudStrategy<Biomaterial> {
    private final @NonNull BiomaterialRepository biomaterialRepository;

    @Override
    public Biomaterial saveMetadataDocument(Biomaterial document) {
        return biomaterialRepository.save(document);
    }

    @Override
    public Biomaterial findMetadataDocument(String id) {
        return biomaterialRepository.findById(id)
                                    .orElseThrow(() -> {
                                        throw new ResourceNotFoundException();
                                    });
    }

    @Override
    public Biomaterial findOriginalByUuid(String uuid) {
        return biomaterialRepository.findByUuidUuidAndIsUpdateFalse(UUID.fromString(uuid))
                                    .orElseThrow(() -> {
                                        throw new ResourceNotFoundException();
                                    });
    }

    @Override
    public Collection<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope);
    }
}