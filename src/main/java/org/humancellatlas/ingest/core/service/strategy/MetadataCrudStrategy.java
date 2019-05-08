package org.humancellatlas.ingest.core.service.strategy;

import org.humancellatlas.ingest.core.MetadataDocument;

public interface MetadataCrudStrategy <T extends MetadataDocument> {
    T saveMetadataDocument(T document);
    T findMetadataDocument(String id);
    T findOriginalByUuid(String uuid);
}