package org.humancellatlas.ingest.project.wranglingpriority;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class WranglingPrioritySerializer extends JsonSerializer <WranglingPriority>{
    @Override
    public void serialize(WranglingPriority wranglingPriority, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        generator.writeString(wranglingPriority.text);
    }
}
