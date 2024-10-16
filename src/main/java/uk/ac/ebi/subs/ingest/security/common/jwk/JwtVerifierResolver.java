package uk.ac.ebi.subs.ingest.security.common.jwk;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

/**
 * Helper class whose main purpose is to create a {@link JWTVerifier} instance given a JWT string.
 *
 * <p>This is part of the wrapper subsystem to help compartmentalise the area of the application
 * that relies on Auth0's library for processing JWTs.
 */
public class JwtVerifierResolver {

  private final JwkVault jwkVault;

  private final Optional<String> audience;
  private final Optional<String> issuer;

  public JwtVerifierResolver(JwkVault jwkVault, String audience, String issuer) {
    this.jwkVault = jwkVault;
    this.audience = Optional.ofNullable(audience);
    this.issuer = Optional.ofNullable(issuer);
  }

  public String getIssuer() {
    return issuer.orElse(null);
  }

  public JWTVerifier resolve(String jwt) {
    DecodedJWT token = JWT.decode(jwt);
    RSAPublicKey publicKey = (RSAPublicKey) jwkVault.getPublicKey(token);
    DelegatingJwtVerifier.Builder builder =
        DelegatingJwtVerifier.require(Algorithm.RSA256(publicKey, null));
    audience.ifPresent(builder::withAudience);
    builder.withIssuer(issuer.orElse(token.getIssuer()));
    return builder.build();
  }
}
