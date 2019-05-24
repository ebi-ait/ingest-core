package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Component;

import java.util.Collection;


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
        return biomaterialRepository.findOne(id);
    }

    @Override
    public Biomaterial findOriginalByUuid(String uuid) {
        return biomaterialRepository.findByUuidAndIsUpdateFalse(new Uuid(uuid));
    }

    @Override
    public Collection<Biomaterial> findBySubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        return biomaterialRepository.findBySubmissionEnvelopesContaining(submissionEnvelope);
    }
}