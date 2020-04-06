package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.authn.provider.elixir.ElixirAaiAuthenticationProvider;
import org.humancellatlas.ingest.security.authn.provider.elixir.ElixirJwkVault;
import org.humancellatlas.ingest.security.common.jwk.RemoteServiceJwtVerifierResolver;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

@Configuration
public class ElixirConfig {

    public static final String ELIXIR = "elixir";

    @Value("${AUTH_ISSUER}/jwk")
    private String issuer;

    @Bean(name=ELIXIR)
    public AuthenticationProvider elixirAuthenticationProvider() {
        var urlJwkProviderResolver = new UrlJwkProviderResolver(issuer);
        var elixirJwkVault = new ElixirJwkVault(urlJwkProviderResolver);
        var elixirJwtVerifierResolver = new RemoteServiceJwtVerifierResolver(elixirJwkVault, null, issuer);
        return new ElixirAaiAuthenticationProvider(elixirJwtVerifierResolver);
    }

}
