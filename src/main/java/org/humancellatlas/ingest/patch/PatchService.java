package org.humancellatlas.ingest.patch;

import java.util.Map;

import org.humancellatlas.ingest.core.JsonPatch;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.MetadataDifferService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Service
@AllArgsConstructor
@Getter
public class PatchService {
  private final @NonNull PatchRepository patchRepository;
  private final @NonNull MetadataDifferService metadataDifferService;

  public <T extends MetadataDocument> Patch<T> storePatch(
      T originalDocument, T updateDocument, SubmissionEnvelope submissionEnvelope) {
    JsonPatch patch = metadataDifferService.generatePatch(originalDocument, updateDocument);
    Patch<T> patchDocument =
        new Patch<>(
            new ObjectMapper().convertValue(patch, Map.class),
            submissionEnvelope,
            originalDocument,
            updateDocument);
    Patch<T> savedPatch = patchRepository.save(patchDocument);
    return savedPatch;
  }
}
