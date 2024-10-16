package uk.ac.ebi.subs.ingest.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import uk.ac.ebi.subs.ingest.project.DataAccessTypesReadConverter;
import uk.ac.ebi.subs.ingest.project.DataAccessTypesWriteConverter;

@Configuration
public class ConvertersConfiguration {
  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(
        Arrays.asList(new DataAccessTypesReadConverter(), new DataAccessTypesWriteConverter()));
  }
}
