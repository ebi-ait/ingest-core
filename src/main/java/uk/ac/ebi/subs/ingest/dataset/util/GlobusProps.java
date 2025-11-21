package uk.ac.ebi.subs.ingest.dataset.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "morphic.globus")
public class GlobusProps {
  private String authUrl;
  private String transferUrl;
  private String collectionId;
  private String basePath; // e.g. /ebi/ftp/private/morphic-transfer
  private String clientId;
  private String clientSecret;
  private String transferScope;
  private String dataAccessScope;

  private String sourceEndpointId; // user / submission source endpoint
  private String remoteProcessingPathPrefix;

  /** If true, use local 'globus' CLI for transfer submission instead of Transfer API. */
  private boolean useCli = false;
}
