package org.humancellatlas.ingest.project.wranglingstate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class WranglingStateDeserializer extends JsonDeserializer<WranglingState> {
    @Override
    public WranglingState deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String status = jsonParser.getValueAsString();

        for (WranglingState state : WranglingState.values()) {
            if (state.text.equals(status)) {
                return state;
            }
        }
        return null;
    }
}
