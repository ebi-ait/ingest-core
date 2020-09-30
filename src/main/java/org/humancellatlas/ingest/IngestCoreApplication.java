package org.humancellatlas.ingest;

import com.github.cloudyrock.spring.v5.EnableMongock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@EnableMongock
@SpringBootApplication
@PropertySources({
        @PropertySource("classpath:application.properties")
})
public class IngestCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestCoreApplication.class, args);
    }

}
