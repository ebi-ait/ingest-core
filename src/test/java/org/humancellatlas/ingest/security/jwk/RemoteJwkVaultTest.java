package org.humancellatlas.ingest.security.jwk;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import org.humancellatlas.ingest.security.JwtGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class RemoteJwkVaultTest {

    @Test
    public void testGetPublicKeyForJwt() throws Exception {
        //given: JWT
        String issuer = "https://humancellatlas.auth0.com";
        JwtGenerator generator = new JwtGenerator(issuer);
        var customClaims = Map.ofEntries(
                entry("https://auth.data.humancellatlas.org/email", "sample@domain.tld")
        );
        var jwt = generator.generate(customClaims);

        //and: JWK from remote service
        Jwk jwk = mock(Jwk.class);
        doReturn(generator.getPublicKey()).when(jwk).getPublicKey();

        //and:
        UrlJwkProvider urlJwkProvider = mock(UrlJwkProvider.class);
        doReturn(jwk).when(urlJwkProvider).get(JwtGenerator.DEFAULT_KEY_ID);

        //and:
        UrlJwkProviderResolver urlJwkProviderResolver = mock(UrlJwkProviderResolver.class);
        doReturn(urlJwkProvider).when(urlJwkProviderResolver).resolve(issuer);

        //and: GoogleServiceJwkVault
        JwkVault jwkVault = new RemoteJwkVault(urlJwkProviderResolver);

        //when:
        var publicKey = jwkVault.getPublicKey(jwt);

        //then:
        assertThat(publicKey).isNotNull();
    }

}
