package org.humancellatlas.ingest.project;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.Nullable;

/**
 * Used to deserialize data types when reading from Mongo
 */
@ReadingConverter()
public class DataAccessTypesReadConverter implements Converter<String, Object> {

    @Override
    @Nullable
    public Object convert(String source) {
        return DataAccessTypes.valueOf(source);
    }
}
