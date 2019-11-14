package org.humancellatlas.ingest.security.jwk;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;

import java.security.PublicKey;

public class RemoteJwkVault implements JwkVault {

    private final UrlJwkProviderResolver urlJwkProviderResolver;

    public RemoteJwkVault(UrlJwkProviderResolver urlJwkProviderResolver) {
        this.urlJwkProviderResolver = urlJwkProviderResolver;
    }

    @Override
    public PublicKey getPublicKey(String jwt) {
        var token = JWT.decode(jwt);
        var issuer = token.getIssuer();
        UrlJwkProvider jwkProvider = urlJwkProviderResolver.resolve(issuer);
        try {
            var jwk = jwkProvider.get(token.getKeyId());
            return jwk.getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(e);
        }
    }

}
