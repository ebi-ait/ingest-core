package uk.ac.ebi.subs.ingest.patch;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.JsonPatch;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.service.MetadataDifferService;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

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
