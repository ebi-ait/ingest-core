package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;

import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.RedundantUpdateException;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class MetadataUpdateService {
    private final @NonNull MetadataDifferService metadataDifferService;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull PatchService patchService;

    public <T extends MetadataDocument> T acceptUpdate(T updateDocument, SubmissionEnvelope submissionEnvelope) {
        T originalDocument = metadataCrudService.findOriginalByUuid(updateDocument.getUuid().getUuid().toString(), updateDocument.getType());

        if(metadataDifferService.anyDifference(originalDocument, updateDocument)) {
            T savedUpdateDocument = metadataCrudService.addToSubmissionEnvelopeAndSave(updateDocument, submissionEnvelope);
            patchService.storePatch(originalDocument, savedUpdateDocument, submissionEnvelope);
            return savedUpdateDocument;
        } else {
            throw new RedundantUpdateException(String.format("Attempted to update %s document at %s with contents of %s but there is no diff",
                                                             updateDocument.getType(),
                                                             originalDocument.getId(),
                                                             updateDocument.getId()));
        }
    }

    public void applyUpdates(SubmissionEnvelope submissionEnvelope) {
        applyUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.BIOMATERIAL));
        applyUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.FILE));
        applyUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROCESS));
        applyUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROJECT));
        applyUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROTOCOL));
    }

    private <T extends MetadataDocument> Stream<T> applyUpdateDocuments(Stream<T> updateDocuments) {
        return updateDocuments
                .map(updateDocument -> {
                    String documentUuid = updateDocument.getUuid().getUuid().toString();
                    EntityType entityType = updateDocument.getType();
                    T originalDocument = metadataCrudService.findOriginalByUuid(documentUuid, entityType);
                    T upsertedDocument = applyUpdateDocument(originalDocument, updateDocument);
                    return metadataCrudService.save(upsertedDocument);
                });
    }

    private <T extends MetadataDocument> T applyUpdateDocument(T canonicalDocument, T updateDocument) {
        canonicalDocument.setDcpVersion(updateDocument.getDcpVersion());
        canonicalDocument.setValidationState(updateDocument.getValidationState());
        canonicalDocument.setContent(updateDocument.getContent());
        return canonicalDocument;
    }
}