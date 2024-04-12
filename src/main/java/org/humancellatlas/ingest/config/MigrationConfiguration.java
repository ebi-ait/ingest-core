package org.humancellatlas.ingest.config;

import com.github.mongobee.Mongobee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfiguration {
    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;

    @Bean
    public Mongobee Configure() {
        Mongobee runner = new Mongobee(mongoURI);
        runner.setChangeLogsScanPackage("org.humancellatlas.ingest.migrations");
        return runner;
    }
}