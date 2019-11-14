package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.humancellatlas.ingest.security.jwk.RemoteJwkVault;

import java.security.interfaces.RSAPublicKey;

public class RemoteServiceJwtVerifierResolver {

    private final RemoteJwkVault jwkVault;

    private final String audience;

    public RemoteServiceJwtVerifierResolver(RemoteJwkVault jwkVault, String audience) {
        this.jwkVault = jwkVault;
        this.audience = audience;
    }

    public JWTVerifier resolve(String jwt) {
        DecodedJWT token = JWT.decode(jwt);
        RSAPublicKey publicKey = (RSAPublicKey) jwkVault.getPublicKey(token);
        return DelegatingJwtVerifier
                .require(Algorithm.RSA256(publicKey, null))
                .withAudience(audience)
                .withIssuer(token.getIssuer())
                .build();
    }
}
