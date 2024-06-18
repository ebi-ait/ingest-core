package org.humancellatlas.ingest.config;

import java.util.Arrays;

import org.humancellatlas.ingest.project.DataAccessTypesReadConverter;
import org.humancellatlas.ingest.project.DataAccessTypesWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class ConvertersConfiguration {
  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(
        Arrays.asList(new DataAccessTypesReadConverter(), new DataAccessTypesWriteConverter()));
  }
}
