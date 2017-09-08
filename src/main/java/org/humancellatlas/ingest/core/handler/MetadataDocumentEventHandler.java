package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.service.AccessionMetadataService;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
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
@AllArgsConstructor
public class MetadataDocumentEventHandler extends AbstractMongoEventListener<BioMetadataDocument> {
    private final @NonNull ValidateMetadataService validateMetadataService;
    private final @NonNull AccessionMetadataService accessionMetadataService;

    @Override
    public void onAfterSave(AfterSaveEvent<BioMetadataDocument> documentAfterSaveEvent){
        BioMetadataDocument document = documentAfterSaveEvent.getSource();

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
