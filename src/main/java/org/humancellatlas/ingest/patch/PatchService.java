package org.humancellatlas.ingest.patch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.MetadataDifferService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Getter
public class PatchService {
    private final @NonNull PatchRepository patchRepository;
    private final @NonNull MetadataDifferService metadataDifferService;

    public <T extends MetadataDocument> Patch<T> storePatch(T originalDocument, T updateDocument, SubmissionEnvelope submissionEnvelope) {
        JsonNode patch = metadataDifferService.generatePatch(originalDocument, updateDocument);
        Patch<T> patchDocument = new Patch<>(patch, submissionEnvelope, originalDocument, updateDocument);
        Patch<T> savedPatch = patchRepository.save(patchDocument);
        return savedPatch;
    }
}
