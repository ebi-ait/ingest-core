package org.humancellatlas.ingest.archiving.entity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ArchiveEntityTypeSerializer extends JsonSerializer<ArchiveEntityType> {

  @Override
  public void serialize(
      ArchiveEntityType value, JsonGenerator generator, SerializerProvider serializers)
      throws IOException {
    generator.writeString(value.type);
  }
}
