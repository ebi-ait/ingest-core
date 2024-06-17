package org.humancellatlas.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@PropertySources({@PropertySource("classpath:application.properties")})
public class IngestCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngestCoreApplication.class, args);
  }
}
