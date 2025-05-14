package uk.ac.ebi.subs.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@PropertySources({@PropertySource("classpath:application.properties")})
@EnableMongoRepositories(basePackages = {
        "uk.ac.ebi.subs.ingest.submission",
        "uk.ac.ebi.subs.ingest.project",
        "uk.ac.ebi.subs.ingest.biomaterial",
        "uk.ac.ebi.subs.ingest.process"
})

public class IngestCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngestCoreApplication.class, args);
  }
}
