package org.humancellatlas.ingest.security.jwk;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

public class RemoteJwkVault implements JwkVault {

    private final UrlJwkProviderResolver urlJwkProviderResolver;

    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

    public RemoteJwkVault(UrlJwkProviderResolver urlJwkProviderResolver) {
        this.urlJwkProviderResolver = urlJwkProviderResolver;
    }

    @Override
    public PublicKey getPublicKey(DecodedJWT jwt) {
        var issuer = jwt.getIssuer();
        log.info(String.format("Getting public key for issues %s", issuer));

        UrlJwkProvider jwkProvider = urlJwkProviderResolver.resolve(issuer);
        log.info(String.format("Using JWK provider url: %s", urlJwkProviderResolver.resolveUrl(issuer)));
        try {
            log.info(String.format("Key id: %s", jwt.getKeyId()));
            var jwk = jwkProvider.get(jwt.getKeyId());
            log.info(String.format("JWK for key id %s: %s", jwt.getKeyId(), jwk.toString()));
            return jwk.getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(e);
        }
    }

}
