package org.humancellatlas.ingest.core.service;

import lombok.AllArgsConstructor;

import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.RedundantUpdateException;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

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

    public void upsertUpdates(SubmissionEnvelope submissionEnvelope) {
        upsertUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.BIOMATERIAL));
        upsertUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.FILE));
        upsertUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROCESS));
        upsertUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROJECT));
        upsertUpdateDocuments(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROTOCOL));
    }

    private <T extends MetadataDocument> Collection<T> upsertUpdateDocuments(Collection<T> updateDocuments) {
        return updateDocuments
                .stream()
                .map(updateDocument -> {
                    String documentUuid = updateDocument.getUuid().getUuid().toString();
                    EntityType entityType = updateDocument.getType();
                    T originalDocument = metadataCrudService.findOriginalByUuid(documentUuid, entityType);
                    T upsertedDocument = doUpsert(originalDocument, updateDocument);
                    return metadataCrudService.save(upsertedDocument);
                })
                .collect(Collectors.toList());
    }

    private <T extends MetadataDocument> T doUpsert(T canonicalDocument, T updateDocument) {
        canonicalDocument.setDcpVersion(updateDocument.getDcpVersion());
        canonicalDocument.setValidationState(updateDocument.getValidationState());
        canonicalDocument.setContent(canonicalDocument.getContent());
        return canonicalDocument;
    }
}