package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.authn.provider.elixir.ElixirAaiAuthenticationProvider;
import org.humancellatlas.ingest.security.authn.provider.elixir.ElixirJwkVault;
import org.humancellatlas.ingest.security.common.jwk.JwkVault;
import org.humancellatlas.ingest.security.common.jwk.JwtVerifierResolver;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;

@Configuration
public class ElixirConfig {

    public static final String ELIXIR = "elixir";

    @Value("${AUTH_ISSUER}")
    private String issuer;

    @Bean(name=ELIXIR)
    public JwtVerifierResolver elixirJwtVerifierResolver() {
        var urlJwkProviderResolver = new UrlJwkProviderResolver(issuer + "/jwk");
        ElixirJwkVault jwkVault = new ElixirJwkVault(urlJwkProviderResolver);
        return new JwtVerifierResolver(jwkVault, null, issuer);
    }

}
