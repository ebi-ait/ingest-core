package org.humancellatlas.ingest.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.exception.RedundantUpdateException;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.patch.PatchService;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Service
public class MetadataUpdateService {
    private final @NonNull MetadataDifferService metadataDifferService;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull PatchService patchService;

    private final @NonNull ValidationStateChangeService validationStateChangeService;
    private final @NonNull JsonPatcher jsonPatcher;

    private final Environment environment;

    public <T extends MetadataDocument> T update(T metadataDocument, ObjectNode patch) {
        ObjectMapper mapper = new ObjectMapper();

        Boolean contentChanged = Optional.ofNullable(patch.get("content"))
                .map(content -> !content.equals(mapper.valueToTree(metadataDocument.getContent())))
                .orElse(false);

        T patchedMetadata = jsonPatcher.merge(patch, metadataDocument);
        T doc = metadataCrudService.save(patchedMetadata);

        /* Don't do for MorPhic - no need for updateStudy or updateDataset for bypassing this*/
        if (!Arrays.asList(environment.getActiveProfiles()).contains("morphic") && contentChanged) {
            validationStateChangeService.changeValidationState(doc.getType(), doc.getId(), ValidationState.DRAFT);
        }

        return doc;
    }

    public <T extends MetadataDocument> T updateStudy(T metadataDocument, ObjectNode patch) {
        ObjectMapper mapper = new ObjectMapper();

        Boolean contentChanged = Optional.ofNullable(patch.get("content"))
                .map(content -> !content.equals(mapper.valueToTree(metadataDocument.getContent())))
                .orElse(false);

        T patchedMetadata = jsonPatcher.merge(patch, metadataDocument);
        T doc = metadataCrudService.save(patchedMetadata);

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

}
