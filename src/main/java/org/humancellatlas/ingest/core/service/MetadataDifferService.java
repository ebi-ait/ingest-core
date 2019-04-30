package org.humancellatlas.ingest.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetadataDifferService {

    public boolean anyDifference(MetadataDocument source, MetadataDocument target) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode sourceContent = objectMapper.valueToTree(source.getContent());
        JsonNode targetContent = objectMapper.valueToTree(target.getContent());

        return ! sourceContent.equals(targetContent);
    }

    public JsonNode generatePatch(JsonNode source, JsonNode target) {
        return JsonDiff.asJson(source, target);
    }

}
