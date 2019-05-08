package org.humancellatlas.ingest.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.humancellatlas.ingest.core.JsonPatch;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.MetadataDifferService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
@Getter
public class PatchService {
    private final @NonNull PatchRepository patchRepository;
    private final @NonNull MetadataDifferService metadataDifferService;

    public <T extends MetadataDocument> Patch<T> storePatch(T originalDocument, T updateDocument, SubmissionEnvelope submissionEnvelope) {
        JsonPatch patch = metadataDifferService.generatePatch(originalDocument, updateDocument);
        Patch<T> patchDocument = new Patch<>(new ObjectMapper().convertValue(patch, Map.class),
                                             submissionEnvelope,
                                             originalDocument,
                                             updateDocument);
        Patch<T> savedPatch = patchRepository.save(patchDocument);
        return savedPatch;
    }
}
