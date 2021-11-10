package org.humancellatlas.ingest.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.web.BiomaterialEventHandler;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.file.web.FileEventHandler;
import org.humancellatlas.ingest.process.ProcessEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguration {
    private final @NonNull ValidationStateChangeService validationStateChangeService;

    @Bean
    ProcessEventHandler processEventHandler() {
        return new ProcessEventHandler(validationStateChangeService);
    }

    @Bean
    BiomaterialEventHandler biomaterialEventHandler() {
        return new BiomaterialEventHandler(validationStateChangeService);
    }

    @Bean
    FileEventHandler fileEventHandler() {
        return new FileEventHandler(validationStateChangeService);
    }
}