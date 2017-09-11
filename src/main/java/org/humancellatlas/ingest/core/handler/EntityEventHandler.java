package org.humancellatlas.ingest.core.handler;

import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by rolando on 11/09/2017.
 */
@Component
@RepositoryEventHandler
public class EntityEventHandler {

    @HandleBeforeSave
    public void beforeSave(AbstractEntity entity){
        if(entity.getUuid() == null ||
           (entity.getUuid() != null && StringUtils.isEmpty(entity.getUuid().getUuid()))){
            assignUuid(entity);
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(AbstractEntity entity) {
        assignUuid(entity);
    }

    private void assignUuid(AbstractEntity entity) {
        entity.setUuid(new Uuid(UUID.randomUUID().toString()));
    }

}
