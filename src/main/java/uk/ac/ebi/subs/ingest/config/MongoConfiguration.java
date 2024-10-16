package uk.ac.ebi.subs.ingest.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import uk.ac.ebi.subs.ingest.project.DataAccessTypesReadConverter;
import uk.ac.ebi.subs.ingest.project.DataAccessTypesWriteConverter;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "userAuditing")
public class MongoConfiguration {

  @Bean
  public CustomConversions customConversions() {

    return new MongoCustomConversions(
        Arrays.asList(new DataAccessTypesReadConverter(), new DataAccessTypesWriteConverter()));
  }
}
