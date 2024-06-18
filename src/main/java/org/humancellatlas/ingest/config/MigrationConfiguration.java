package org.humancellatlas.ingest.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfiguration {
  /*@Value("${spring.data.mongodb.uri}")
  private String mongoURI;

  @Bean
  public Mongobee Configure() {
      Mongobee runner = new Mongobee(mongoURI);
      runner.setChangeLogsScanPackage("org.humancellatlas.ingest.migrations");
      return runner;
  }*/
}
