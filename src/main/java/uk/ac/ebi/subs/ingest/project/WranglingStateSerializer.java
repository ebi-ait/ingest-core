package uk.ac.ebi.subs.ingest.project;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class WranglingStateSerializer extends JsonSerializer<WranglingState> {
  @Override
  public void serialize(
      WranglingState value, JsonGenerator generator, SerializerProvider serializers)
      throws IOException {
    generator.writeString(value.value);
  }
}
