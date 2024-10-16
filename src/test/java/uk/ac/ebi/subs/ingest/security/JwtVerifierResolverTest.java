package uk.ac.ebi.subs.ingest.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import uk.ac.ebi.subs.ingest.security.authn.provider.gcp.GcpJwkVault;
import uk.ac.ebi.subs.ingest.security.common.jwk.DelegatingJwtVerifier;
import uk.ac.ebi.subs.ingest.security.common.jwk.JwtVerifierResolver;

public class JwtVerifierResolverTest {

  @Test
  public void testResolveForJwt() {
    // given:
    JwtGenerator jwtGenerator = new JwtGenerator("issuerFromToken");
    RSAPublicKey publicKey = jwtGenerator.getPublicKey();

    // and:
    String audience = "https://dev.data.humancellatlas.org/";
    GcpJwkVault jwkVault = mock(GcpJwkVault.class);
    doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

    // and:
    JwtVerifierResolver jwtVerifierResolver = new JwtVerifierResolver(jwkVault, audience, null);

    // and: given the token
    String jwt = jwtGenerator.generate();
    DecodedJWT token = JWT.decode(jwt);

    // when:
    JWTVerifier verifier = jwtVerifierResolver.resolve(jwt);

    // then:
    assertThat(verifier).isNotNull();

    // and: inspect using verifier with extended interface as a work around
    assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
    DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
    assertThat(delegatingVerifier)
        .extracting("audience", "issuer")
        .containsExactly(audience, token.getIssuer());
  }

  @Test
  public void testResolveForJwtWithIssuer() {
    // given:
    String issuerFromToken = "issuerFromToken";
    JwtGenerator jwtGenerator = new JwtGenerator(issuerFromToken);
    RSAPublicKey publicKey = jwtGenerator.getPublicKey();

    // and:
    String audience = "https://dev.data.humancellatlas.org/";
    String issuer = "auth0";
    GcpJwkVault jwkVault = mock(GcpJwkVault.class);
    doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

    // and:
    JwtVerifierResolver jwtVerifierResolver = new JwtVerifierResolver(jwkVault, audience, issuer);

    // and: given the token
    String jwt = jwtGenerator.generate();

    // when:
    JWTVerifier verifier = jwtVerifierResolver.resolve(jwt);

    // then:
    assertThat(verifier).isNotNull();

    // and: inspect using verifier with extended interface as a work around
    assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
    DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
    assertThat(delegatingVerifier)
        .extracting("audience", "issuer")
        .containsExactly(audience, issuer);
  }

  @Test
  public void testResolveForJwtWithNoAudience() {
    // given:
    JwtGenerator jwtGenerator = new JwtGenerator();
    RSAPublicKey publicKey = jwtGenerator.getPublicKey();

    // and:
    String issuer = "auth0";
    GcpJwkVault jwkVault = mock(GcpJwkVault.class);
    doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

    // and:
    JwtVerifierResolver jwtVerifierResolver = new JwtVerifierResolver(jwkVault, null, issuer);

    // and: given the token
    String jwt = jwtGenerator.generate();

    // when:
    JWTVerifier verifier = jwtVerifierResolver.resolve(jwt);

    // then:
    assertThat(verifier).isNotNull();

    // and: inspect using verifier with extended interface as a work around
    assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
    DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
    assertThat(delegatingVerifier).extracting("audience", "issuer").containsExactly(null, issuer);
  }

  @Test
  public void testResolveForJwtWithNoAudienceAndNoIssuer() {
    // given:
    String issuerFromToken = "issuerFromToken";
    JwtGenerator jwtGenerator = new JwtGenerator(issuerFromToken);
    RSAPublicKey publicKey = jwtGenerator.getPublicKey();

    // and:
    String issuer = "auth0";
    GcpJwkVault jwkVault = mock(GcpJwkVault.class);
    doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

    // and:
    JwtVerifierResolver jwtVerifierResolver = new JwtVerifierResolver(jwkVault, null, null);

    // and: given the token
    String jwt = jwtGenerator.generate();
    DecodedJWT token = JWT.decode(jwt);

    // when:
    JWTVerifier verifier = jwtVerifierResolver.resolve(jwt);

    // then:
    assertThat(verifier).isNotNull();

    // and: inspect using verifier with extended interface as a work around
    assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
    DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
    assertThat(delegatingVerifier)
        .extracting("audience", "issuer")
        .containsExactly(null, token.getIssuer());
  }

  @Test
  public void testResolveForJwtWithAudienceAndNoIssuer() {
    // given:
    JwtGenerator jwtGenerator = new JwtGenerator("issuerFromToken");
    RSAPublicKey publicKey = jwtGenerator.getPublicKey();

    // and:
    String audience = "https://dev.data.humancellatlas.org/";
    GcpJwkVault jwkVault = mock(GcpJwkVault.class);
    doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

    // and:
    JwtVerifierResolver jwtVerifierResolver = new JwtVerifierResolver(jwkVault, audience, null);

    // and: given the token
    String jwt = jwtGenerator.generate();
    DecodedJWT token = JWT.decode(jwt);

    // when:
    JWTVerifier verifier = jwtVerifierResolver.resolve(jwt);

    // then:
    assertThat(verifier).isNotNull();

    // and: inspect using verifier with extended interface as a work around
    assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
    DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
    assertThat(delegatingVerifier)
        .extracting("audience", "issuer")
        .containsExactly(audience, token.getIssuer());
  }
}
