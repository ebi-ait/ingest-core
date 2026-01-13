package uk.ac.ebi.subs.ingest.dataset.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsSsmConfig {

  @Bean
  public SsmClient ssmClient() {
    // Uses DefaultCredentialsProvider + default region chain unless we override region here.
    // In AWS (EC2/ECS), it will pick up instance role creds automatically.
    return SsmClient.builder()
        .region(Region.EU_WEST_2) // or make this configurable (see below)
        .build();
  }
}
