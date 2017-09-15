package org.humancellatlas.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class IngestCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestCoreApplication.class, args);
    }
}
