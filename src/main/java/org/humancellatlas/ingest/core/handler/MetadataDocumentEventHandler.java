package org.humancellatlas.ingest.core.handler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.ValidationState;
import org.humancellatlas.ingest.core.service.AccessionMetadataService;
import org.humancellatlas.ingest.core.service.ValidateMetadataService;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by rolando on 06/09/2017.
 */
@Component
@AllArgsConstructor
public class MetadataDocumentEventHandler extends AbstractMongoEventListener<MetadataDocument> {
    private final @NonNull ValidateMetadataService validateMetadataService;
    private final @NonNull AccessionMetadataService accessionMetadataService;

    @Override
    public void onAfterSave(AfterSaveEvent<MetadataDocument> documentAfterSaveEvent) {
        MetadataDocument document = documentAfterSaveEvent.getSource();

        doValidation(document);
        doAccession(document);
    }

    private void doValidation(MetadataDocument document) {
        validateMetadataService.validateMetadata(document);
        document.setValidationState(ValidationState.VALIDATING);
    }

    private void doAccession(MetadataDocument document) {
        if (StringUtils.isEmpty(document.getAccession()) &&
                document.getValidationState().equals(ValidationState.VALID)) {
            accessionMetadataService.accessionMetadata(document);
        }
    }
}
