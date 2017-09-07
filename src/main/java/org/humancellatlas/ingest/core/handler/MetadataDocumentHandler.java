package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
@RepositoryEventHandler
@AllArgsConstructor
public class MetadataDocumentHandler {
    private final @NonNull ValidateMetadataService validateMetadataService;

    @HandleBeforeCreate
    public void assignUuid(AbstractEntity entity) {
        entity.setUuid(new Uuid(UUID.randomUUID().toString()));
    }
}
