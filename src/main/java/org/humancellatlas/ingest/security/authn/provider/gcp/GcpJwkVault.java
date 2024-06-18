package org.humancellatlas.ingest.security.authn.provider.gcp;

import java.security.PublicKey;

import org.humancellatlas.ingest.security.common.jwk.JwkVault;
import org.humancellatlas.ingest.security.common.jwk.UrlJwkProviderResolver;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.interfaces.DecodedJWT;

public class GcpJwkVault implements JwkVault {

  private final UrlJwkProviderResolver urlJwkProviderResolver;

  public GcpJwkVault(UrlJwkProviderResolver urlJwkProviderResolver) {
    this.urlJwkProviderResolver = urlJwkProviderResolver;
  }

  @Override
  public PublicKey getPublicKey(DecodedJWT jwt) {
    var issuer = jwt.getIssuer();
    UrlJwkProvider jwkProvider = urlJwkProviderResolver.resolve(issuer);
    try {
      var jwk = jwkProvider.get(jwt.getKeyId());
      return jwk.getPublicKey();
    } catch (JwkException e) {
      throw new RuntimeException(e);
    }
  }
}
