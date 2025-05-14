package uk.ac.ebi.subs.ingest.gene;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "uk.ac.ebi.subs.ingest.gene",
        mongoTemplateRef = "geneMongoTemplate"
)
public class GeneDbRepoConfig {
}
