package uk.ac.ebi.subs.ingest.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

import uk.ac.ebi.subs.ingest.security.authn.provider.gcp.GcpDomainWhiteList;
import uk.ac.ebi.subs.ingest.security.authn.provider.gcp.GcpJwkVault;
import uk.ac.ebi.subs.ingest.security.authn.provider.gcp.GoogleServiceJwtAuthenticationProvider;
import uk.ac.ebi.subs.ingest.security.common.jwk.JwtVerifierResolver;
import uk.ac.ebi.subs.ingest.security.common.jwk.UrlJwkProviderResolver;

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
