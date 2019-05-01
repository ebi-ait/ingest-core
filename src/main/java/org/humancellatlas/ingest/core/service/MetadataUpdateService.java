package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;

import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.RedundantUpdateException;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class MetadataUpdateService {
    private final @NonNull MetadataDifferService metadataDifferService;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull PatchService patchService;

    public <T extends MetadataDocument> T acceptUpdate(T updateDocument, SubmissionEnvelope submissionEnvelope) {
        String documentType = updateDocument.getType().toString();
        MetadataCrudStrategy metadataCrudStrategy = metadataCrudService.crudStrategyForMetadataType(updateDocument.getType().toString());
        T originalDocument = (T) metadataCrudStrategy.findOriginalByUuid(updateDocument.getUuid().getUuid().toString());

        if(metadataDifferService.anyDifference(originalDocument, updateDocument)) {
            patchService.storePatch(originalDocument, updateDocument, submissionEnvelope);
            return metadataCrudService.addToSubmissionEnvelopeAndSave(updateDocument, submissionEnvelope);
        } else {
            throw new RedundantUpdateException(String.format("Attempted to update %s document at %s with contents of %s but there is no diff",
                                                             documentType,
                                                             originalDocument.getId(),
                                                             updateDocument.getId()));
        }
    }

}