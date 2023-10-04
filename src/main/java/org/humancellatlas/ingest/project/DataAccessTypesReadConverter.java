package org.humancellatlas.ingest.project;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter()
public class DataAccessTypesReadConverter implements Converter<String, Object> {

    @Override
    public Object convert(String source) {
        return DataAccessTypes.valueOf(source);
    }
}
