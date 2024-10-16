package uk.ac.ebi.subs.ingest.project;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serialises {@link DataAccessTypes} enum to a JSON value. Used when converting an object to JSON
 * using {@link com.fasterxml.jackson.databind.ObjectMapper}
 */
public class DataAccessTypesJsonSerializer extends StdSerializer<DataAccessTypes> {
  public DataAccessTypesJsonSerializer() {
    this(null);
  }

  public DataAccessTypesJsonSerializer(Class<DataAccessTypes> t) {
    super(t);
  }

  @Override
  public void serialize(DataAccessTypes value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    //        gen.writeStartObject();
    //        gen.writeFieldName("type");
    gen.writeString(value.getLabel());
    //        gen.writeEndObject();
  }
}
