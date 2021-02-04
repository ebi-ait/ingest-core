package org.humancellatlas.ingest.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.RedundantUpdateException;
import org.humancellatlas.ingest.core.service.strategy.MetadataCrudStrategy;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import java.util.Collection;

@AllArgsConstructor
@Service
public class MetadataUpdateService {
    private final @NonNull MetadataDifferService metadataDifferService;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull PatchService patchService;

    private final @NonNull ValidationStateChangeService validationStateChangeService;
    private final @NonNull JsonPatcher jsonPatcher;

    public <T extends MetadataDocument> T update(T metadataDocument, ObjectNode patch) {
        ObjectMapper mapper = new ObjectMapper();

        Boolean contentChanged = patch.get("content") != null &&
                !patch.get("content").equals(mapper.valueToTree(metadataDocument).get("content"));

        T patchedMetadata = jsonPatcher.merge(patch, metadataDocument);
        T doc = metadataCrudService.save(patchedMetadata);

        if (contentChanged) {
            validationStateChangeService.changeValidationState(doc.getType(), doc.getId(), ValidationState.DRAFT);
        }

        return doc;
    }

    public <T extends MetadataDocument> T acceptUpdate(T updateDocument, SubmissionEnvelope submissionEnvelope) {
        T originalDocument = metadataCrudService.findOriginalByUuid(updateDocument.getUuid().getUuid().toString(), updateDocument.getType());

        if (metadataDifferService.anyDifference(originalDocument, updateDocument)) {
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
        applyUpdateDocuments(metadataCrudService.findAllBySubmission(submissionEnvelope, EntityType.BIOMATERIAL));
        applyUpdateDocuments(metadataCrudService.findAllBySubmission(submissionEnvelope, EntityType.FILE));
        applyUpdateDocuments(metadataCrudService.findAllBySubmission(submissionEnvelope, EntityType.PROCESS));
        applyUpdateDocuments(metadataCrudService.findAllBySubmission(submissionEnvelope, EntityType.PROJECT));
        applyUpdateDocuments(metadataCrudService.findAllBySubmission(submissionEnvelope, EntityType.PROTOCOL));
    }

    private <T extends MetadataDocument> void applyUpdateDocuments(Collection<T> documents) {
        for (T updateDocument : documents) {
            String documentUuid = updateDocument.getUuid().getUuid().toString();
            EntityType entityType = updateDocument.getType();
            T originalDocument = metadataCrudService.findOriginalByUuid(documentUuid, entityType);
            T upsertedDocument = applyUpdateDocument(originalDocument, updateDocument);
            metadataCrudService.save(upsertedDocument);
        }
    }

    private <T extends MetadataDocument> T applyUpdateDocument(T canonicalDocument, T updateDocument) {
        canonicalDocument.setDcpVersion(updateDocument.getDcpVersion());
        canonicalDocument.setValidationState(updateDocument.getValidationState());
        canonicalDocument.setContent(updateDocument.getContent());
        return canonicalDocument;
    }

}