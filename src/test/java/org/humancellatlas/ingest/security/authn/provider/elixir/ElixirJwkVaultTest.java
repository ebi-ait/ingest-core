package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import org.humancellatlas.ingest.security.JwtGenerator;
import org.humancellatlas.ingest.security.common.jwk.JwkVault;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ElixirJwkVaultTest {

    @Test
    public void testGetPublicKey() throws Exception {
        //given: JWT
        String issuer = "https://login.elixir-czech.org/oidc";
        JwtGenerator generator = new JwtGenerator(issuer);
        var jwt = generator.generate();

        Jwk jwk = mock(Jwk.class);
        doReturn(generator.getPublicKey()).when(jwk).getPublicKey();

        //and:
        UrlJwkProvider urlJwkProvider = mock(UrlJwkProvider.class);
        doReturn(jwk).when(urlJwkProvider).get(JwtGenerator.DEFAULT_KEY_ID);

        //and:
        UrlJwkProviderResolver urlJwkProviderResolver = mock(UrlJwkProviderResolver.class);
        doReturn(urlJwkProvider).when(urlJwkProviderResolver).resolve();

        //and:
        JwkVault jwkVault = new ElixirJwkVault(urlJwkProviderResolver);

        //when:
        var token = JWT.decode(jwt);
        var publicKey = jwkVault.getPublicKey(token);

        //then:
        assertThat(publicKey).isNotNull();
    }
}
