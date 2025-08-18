package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.authn.provider.elixir.ElixirJwkVault;
import org.humancellatlas.ingest.security.common.jwk.JwtVerifierResolver;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class ElixirConfig {

    public static final String ELIXIR = "elixir";

    @Value("${AUTH_ISSUER}")
    private String issuer;

    @Bean
    @Qualifier(ELIXIR)
    public JwtVerifierResolver elixirJwtVerifierResolver() {
        String jwkUrl = UriComponentsBuilder.fromUriString(issuer).pathSegment("jwk").toUriString();
        var urlJwkProviderResolver = new UrlJwkProviderResolver(jwkUrl);
        ElixirJwkVault jwkVault = new ElixirJwkVault(urlJwkProviderResolver);
        return new JwtVerifierResolver(jwkVault, null, issuer);
    }

}
