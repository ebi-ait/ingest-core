package uk.ac.ebi.subs.ingest.config;

import java.util.Arrays;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import uk.ac.ebi.subs.ingest.project.DataAccessTypesReadConverter;
import uk.ac.ebi.subs.ingest.project.DataAccessTypesWriteConverter;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "userAuditing")
public class MongoConfiguration {

  @Value("${spring.data.mongodb.uri}")
  private String defaultMongoUri;

  @Bean
  public MongoClient defaultMongoClient() {
    return MongoClients.create(defaultMongoUri);
  }

  @Bean(name = "mongoTemplate")
  public MongoTemplate mongoTemplate() {
    return new MongoTemplate(defaultMongoClient(), "MorphicDev"); // or whatever your main DB is
  }

  @Bean
  public CustomConversions customConversions() {
    return new MongoCustomConversions(
            Arrays.asList(new DataAccessTypesReadConverter(), new DataAccessTypesWriteConverter()));
  }
}
