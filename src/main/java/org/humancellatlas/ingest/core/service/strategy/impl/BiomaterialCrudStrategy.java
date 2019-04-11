package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BiomaterialCrudStrategy implements MetadataCrudStrategy<Biomaterial> {
    private final @NonNull
    BiomaterialService biomaterialService;

    @Override
    public Biomaterial saveMetadataDocument(Biomaterial document) {
        return biomaterialService.getBiomaterialRepository().save(document);
    }

    @Override
    public Biomaterial findMetadataDocument(String id) {
        return biomaterialService.getBiomaterialRepository().findById(id).get();
    }
}