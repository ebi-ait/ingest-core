package org.humancellatlas.ingest.project.wranglingpriority;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class WranglingPriorityDeserializer extends JsonDeserializer<WranglingPriority> {
    @Override
    public WranglingPriority deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String value = jsonParser.getValueAsString();

        for (WranglingPriority priority : WranglingPriority.values()) {
            if (priority.text.equals(value)) {
                return priority;
            }
        }

        return null;
    }
}
