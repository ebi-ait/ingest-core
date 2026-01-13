package uk.ac.ebi.subs.ingest.dataset.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "morphic.staging")
public class S3StagingProperties {
    // S3 staging
    private String bucket;
    private String region = "eu-west-2";
    private int presignExpirySeconds = 900;

    // Runner/SSM
    private String runnerInstanceId;          // morphic.staging.runner-instance-id
    private String ssmDocumentName = "AWS-RunShellScript";
    private String stagingBaseDir = "/home/ec2-user/staging";

    // Globus on runner
    private String globusCli = "/home/ec2-user/.local/bin/globus";
    private String globusRunAsUser = "ec2-user";

    // Promote endpoints
    private String srcEndpointId;
    private String destEndpointId;
}
