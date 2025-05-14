package uk.ac.ebi.subs.ingest.gene;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class GeneDbConfig {

    @Value("${gene.mongodb.uri}")
    private String geneMongoUri;

    @Bean
    public MongoClient geneMongoClient() {
        return MongoClients.create(geneMongoUri);
    }

    @Bean(name = "geneMongoTemplate")
    public MongoTemplate geneMongoTemplate() {
        return new MongoTemplate(geneMongoClient(), "GeneDBV2");
    }
}
