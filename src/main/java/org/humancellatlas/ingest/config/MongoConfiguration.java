package org.humancellatlas.ingest.config;

import org.humancellatlas.ingest.project.DataAccessTypesReadConverter;
import org.humancellatlas.ingest.project.DataAccessTypesWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "userAuditing")
public class MongoConfiguration {



    @Bean
    public CustomConversions customConversions() {

        return new MongoCustomConversions(
                Arrays.asList(
                        new DataAccessTypesReadConverter(),
                        new DataAccessTypesWriteConverter()
                )
        );
    }
}
