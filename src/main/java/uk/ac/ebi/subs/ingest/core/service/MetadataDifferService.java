package uk.ac.ebi.subs.ingest.core.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import lombok.AllArgsConstructor;
import uk.ac.ebi.subs.ingest.core.JsonPatch;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;

@Service
@AllArgsConstructor
public class MetadataDifferService {

  public boolean anyDifference(MetadataDocument source, MetadataDocument target) {
    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode sourceContent = objectMapper.valueToTree(source.getContent());
    JsonNode targetContent = objectMapper.valueToTree(target.getContent());

    return !sourceContent.equals(targetContent);
  }

  public <T extends MetadataDocument> JsonPatch generatePatch(
      T originalDocument, T updateDocument) {
    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode sourceContent = objectMapper.valueToTree(originalDocument.getContent());
    JsonNode targetContent = objectMapper.valueToTree(updateDocument.getContent());

    return this.generatePatch(sourceContent, targetContent);
  }

  public JsonPatch generatePatch(JsonNode source, JsonNode target) {
    return new JsonPatch(JsonDiff.asJson(source, target));
  }
}
