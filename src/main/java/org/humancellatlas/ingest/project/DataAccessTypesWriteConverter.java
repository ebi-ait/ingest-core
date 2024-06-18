package org.humancellatlas.ingest.project;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.Nullable;

/** Used to deserialize data types when reading from Mongo */
@WritingConverter()
public class DataAccessTypesWriteConverter implements Converter<DataAccessTypes, String> {

  @Override
  @Nullable
  public String convert(DataAccessTypes source) {
    return source.getLabel();
  }
}
