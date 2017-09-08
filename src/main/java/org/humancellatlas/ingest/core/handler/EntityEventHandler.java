package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
public class EntityEventHandler extends AbstractMongoEventListener<AbstractEntity> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<AbstractEntity> entityBeforeSaveEvent) {
        entityBeforeSaveEvent.getSource().setUuid(new Uuid(UUID.randomUUID().toString()));
    }
}


