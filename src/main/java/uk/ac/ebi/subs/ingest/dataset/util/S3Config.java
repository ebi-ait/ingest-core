package uk.ac.ebi.subs.ingest.dataset.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Bean
    public AmazonS3 amazonS3(S3StagingProperties props) {
        return AmazonS3ClientBuilder.standard()
                .withRegion(props.getRegion())
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}
