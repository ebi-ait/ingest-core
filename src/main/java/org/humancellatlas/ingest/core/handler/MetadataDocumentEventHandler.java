package org.humancellatlas.ingest.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.service.AccessionMetadataService;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
@AllArgsConstructor
@RepositoryEventHandler
public class MetadataDocumentEventHandler{
    private final @NonNull ValidateMetadataService validateMetadataService;
    private final @NonNull AccessionMetadataService accessionMetadataService;

    @HandleAfterCreate
    public void onAfterCreate(BioMetadataDocument document){
        doAccession(document);
    }

    @HandleAfterSave
    public void onAfterSave(BioMetadataDocument document){
        doAccession(document);
    }

    @HandleBeforeCreate
    public void onBeforeCreate(MetadataDocument document) {
        doValidation(document);
    }

    @HandleBeforeCreate
    public void onBeforeCreate(BioMetadataDocument document) {
        if (! validChecksum(document)) {
            doValidation(document);
        }
    }

    @HandleBeforeSave
    public void handleBeforeSave(MetadataDocument document) {
        if (! validChecksum(document)) {
            doValidation(document);
        }
    }

    private void doValidation(MetadataDocument document){
        validateMetadataService.validateMetadata(document);
        document.setValidationStatus(ValidationStatus.VALIDATING);
    }

    private void doAccession(BioMetadataDocument document) {
        if(StringUtils.isEmpty(document.getAccession()) && document.getValidationStatus().equals(ValidationStatus.VALID)) {
            accessionMetadataService.accessionMetadata(document);
        }
    }

    private boolean validChecksum(MetadataDocument document) {
        try {
            if(document.getContent() != null) {
                byte[] contentAsBytes = new ObjectMapper().writeValueAsString(document.getContent()).getBytes();

                return Arrays.equals(DigestUtils.md5Digest(contentAsBytes), document.getValidationChecksum().getMd5().getBytes());
            }

            return  false;
        } catch (IOException e){
            // TODO log
            return false;
        }
    }
}
