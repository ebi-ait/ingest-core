package org.humancellatlas.ingest.core.service.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProtocolCrudStrategy implements MetadataCrudStrategy<Protocol> {
    private final @NonNull
    ProtocolService protocolService;

    @Override
    public Protocol saveMetadataDocument(Protocol document) {
        return protocolService.getProtocolRepository().save(document);
    }

    @Override
    public Protocol findMetadataDocument(String id) {
        return protocolService.getProtocolRepository().findById(id).get();
    }
}