package uk.ac.ebi.subs.ingest.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.exception.RedundantUpdateException;
import uk.ac.ebi.subs.ingest.patch.JsonPatcher;
import uk.ac.ebi.subs.ingest.patch.PatchService;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

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

    Boolean contentChanged =
        Optional.ofNullable(patch.get("content"))
            .map(content -> !content.equals(mapper.valueToTree(metadataDocument.getContent())))
            .orElse(false);

    T patchedMetadata = jsonPatcher.merge(patch, metadataDocument);
    T doc = metadataCrudService.save(patchedMetadata);

    if (contentChanged) {
      validationStateChangeService.changeValidationState(
          doc.getType(), doc.getId(), ValidationState.DRAFT);
    }

    return doc;
  }

  public <T extends MetadataDocument> T acceptUpdate(
      T updateDocument, SubmissionEnvelope submissionEnvelope) {
    T originalDocument =
        metadataCrudService.findOriginalByUuid(
            updateDocument.getUuid().getUuid().toString(), updateDocument.getType());

    if (metadataDifferService.anyDifference(originalDocument, updateDocument)) {
      T savedUpdateDocument =
          metadataCrudService.addToSubmissionEnvelopeAndSave(updateDocument, submissionEnvelope);
      patchService.storePatch(originalDocument, savedUpdateDocument, submissionEnvelope);
      return savedUpdateDocument;
    } else {
      throw new RedundantUpdateException(
          String.format(
              "Attempted to update %s document at %s with contents of %s but there is no diff",
              updateDocument.getType(), originalDocument.getId(), updateDocument.getId()));
    }
  }
}