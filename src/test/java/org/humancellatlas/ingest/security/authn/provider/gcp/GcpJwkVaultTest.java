package org.humancellatlas.ingest.security.authn.provider.gcp;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.humancellatlas.ingest.security.JwtGenerator;
import org.humancellatlas.ingest.security.common.jwk.JwkVault;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;
import org.junit.jupiter.api.Test;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;

public class GcpJwkVaultTest {

  @Test
  public void testGetPublicKeyForJwt() throws Exception {
    // given: JWT
    String issuer = "https://humancellatlas.auth0.com";
    JwtGenerator generator = new JwtGenerator(issuer);
    var customClaims =
        Map.ofEntries(entry("https://auth.data.humancellatlas.org/email", "sample@domain.tld"));
    var jwt = generator.generate(customClaims);

    // and: JWK from remote service
    Jwk jwk = mock(Jwk.class);
    doReturn(generator.getPublicKey()).when(jwk).getPublicKey();

    // and:
    UrlJwkProvider urlJwkProvider = mock(UrlJwkProvider.class);
    doReturn(jwk).when(urlJwkProvider).get(JwtGenerator.DEFAULT_KEY_ID);

    // and:
    UrlJwkProviderResolver urlJwkProviderResolver = mock(UrlJwkProviderResolver.class);
    doReturn(urlJwkProvider).when(urlJwkProviderResolver).resolve(issuer);

    // and: GoogleServiceJwkVault
    JwkVault jwkVault = new GcpJwkVault(urlJwkProviderResolver);

    // when:
    var token = JWT.decode(jwt);
    var publicKey = jwkVault.getPublicKey(token);

    // then:
    assertThat(publicKey).isNotNull();
  }
}
