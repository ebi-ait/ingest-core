package uk.ac.ebi.subs.ingest.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.subs.ingest.security.authn.provider.elixir.ElixirJwkVault;
import uk.ac.ebi.subs.ingest.security.common.jwk.JwtVerifierResolver;
import uk.ac.ebi.subs.ingest.security.common.jwk.UrlJwkProviderResolver;

@Configuration
public class ElixirConfig {

  public static final String ELIXIR = "elixir";

  @Value("${AUTH_ISSUER}")
  private String issuer;

  @Bean
  @Qualifier(ELIXIR)
  public JwtVerifierResolver elixirJwtVerifierResolver() {
    var urlJwkProviderResolver = new UrlJwkProviderResolver(issuer + "/jwk");
    ElixirJwkVault jwkVault = new ElixirJwkVault(urlJwkProviderResolver);
    return new JwtVerifierResolver(jwkVault, null, issuer);
  }
}
