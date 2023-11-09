package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
/**
 * Deserialises {@link DataAccessTypes} JSON value. to enum Used when
 * reading json input.
 */
public class DataAccessTypesJsonDeserializer extends StdDeserializer<DataAccessTypes> {
    public DataAccessTypesJsonDeserializer() {
        this(null);
    }

    public DataAccessTypesJsonDeserializer(Class<Object> t) {
        super(t);
    }

    @Override
    public DataAccessTypes deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//        p.nextToken();
//        p.nextToken();
        String source = p.getText();
        return DataAccessTypes.fromLabel(source);
    }
}

