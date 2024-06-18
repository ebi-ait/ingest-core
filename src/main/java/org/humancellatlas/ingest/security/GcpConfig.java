package org.humancellatlas.ingest.security;

import java.util.List;

import org.humancellatlas.ingest.security.authn.provider.gcp.GcpDomainWhiteList;
import org.humancellatlas.ingest.security.authn.provider.gcp.GcpJwkVault;
import org.humancellatlas.ingest.security.authn.provider.gcp.GoogleServiceJwtAuthenticationProvider;
import org.humancellatlas.ingest.security.common.jwk.JwtVerifierResolver;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

@Configuration
public class GcpConfig {

  public static final String GCP = "gcp";

  @Value("${GCP_JWK_PROVIDER_BASE_URL}")
  private String googleJwkProviderBaseUrl;

  @Value(value = "${SVC_AUTH_AUDIENCE}")
  private String serviceAudience;

  @Value(value = "#{('${GCP_PROJECT_WHITELIST}').split(',')}")
  private List<String> projectWhitelist;

  @Bean(name = GCP)
  public AuthenticationProvider gcpAuthenticationProvider() {
    var urlJwkProviderResolver = new UrlJwkProviderResolver(googleJwkProviderBaseUrl);
    var googleJwkVault = new GcpJwkVault(urlJwkProviderResolver);
    var googleJwtVerifierResolver = new JwtVerifierResolver(googleJwkVault, serviceAudience, null);
    return new GoogleServiceJwtAuthenticationProvider(
        new GcpDomainWhiteList(projectWhitelist), googleJwtVerifierResolver);
  }
}
