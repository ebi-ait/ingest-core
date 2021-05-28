package org.humancellatlas.ingest.project.wranglingstate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class WranglingStateSerializer extends JsonSerializer<WranglingState> {
    @Override
    public void serialize(WranglingState value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeString(value.text);
    }
}
