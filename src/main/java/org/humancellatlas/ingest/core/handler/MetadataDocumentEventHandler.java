package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.service.AccessionMetadataService;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
@AllArgsConstructor
@RepositoryEventHandler
public class MetadataDocumentEventHandler{
    private final @NonNull ValidateMetadataService validateMetadataService;
    private final @NonNull AccessionMetadataService accessionMetadataService;

    @HandleAfterSave
    public void onAfterSave(BioMetadataDocument document){
        doValidation(document);
        doAccession(document);
    }

    private void doValidation(BioMetadataDocument document){
        validateMetadataService.validateMetadata(document);
        document.setValidationStatus(ValidationStatus.VALIDATING);
    }

    private void doAccession(BioMetadataDocument document) {
        if(StringUtils.isEmpty(document.getAccession()) && document.getValidationStatus().equals(ValidationStatus.VALID)) {
            accessionMetadataService.accessionMetadata(document);
        }
    }
}
