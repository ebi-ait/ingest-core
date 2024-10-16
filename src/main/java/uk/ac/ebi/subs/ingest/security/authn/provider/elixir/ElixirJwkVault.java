package uk.ac.ebi.subs.ingest.security.authn.provider.elixir;

import java.security.PublicKey;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.interfaces.DecodedJWT;

import uk.ac.ebi.subs.ingest.security.common.jwk.JwkVault;
import uk.ac.ebi.subs.ingest.security.common.jwk.UrlJwkProviderResolver;

public class ElixirJwkVault implements JwkVault {

  private final UrlJwkProviderResolver urlJwkProviderResolver;

  public ElixirJwkVault(UrlJwkProviderResolver urlJwkProviderResolver) {
    this.urlJwkProviderResolver = urlJwkProviderResolver;
  }

  @Override
  public PublicKey getPublicKey(DecodedJWT jwt) {
    UrlJwkProvider jwkProvider = urlJwkProviderResolver.resolve();
    try {
      var jwk = jwkProvider.get(jwt.getKeyId());
      return jwk.getPublicKey();
    } catch (JwkException e) {
      throw new RuntimeException(e);
    }
  }
}
