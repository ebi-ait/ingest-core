package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.service.AccessionMetadataService;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
@RepositoryEventHandler
@AllArgsConstructor
public class MetadataDocumentHandler {
    private final @NonNull ValidateMetadataService validateMetadataService;
    private final @NonNull AccessionMetadataService accessionMetadataService;

    @HandleBeforeCreate
    public void assignUuid(AbstractEntity entity) {
        entity.setUuid(new Uuid(UUID.randomUUID().toString()));
    }

    @HandleAfterCreate
    public void validateDocument(BioMetadataDocument document){
        validateMetadataService.validateMetadata(document);
        document.setValidationStatus(ValidationStatus.VALIDATING);
    }

    @HandleAfterSave
    public void accessionMetadata(BioMetadataDocument document) {
        if(StringUtils.isEmpty(document.getAccession().getNumber()) && document.getValidationStatus().equals(ValidationStatus.VALID)) {
            accessionMetadataService.accessionMetadata(document);
        }
    }
}
