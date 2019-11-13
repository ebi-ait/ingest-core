package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

public class GoogleServiceJwtVerifierResolver {

    private final GoogleServiceJwkVault jwkVault;

    private final String audience;

    public GoogleServiceJwtVerifierResolver(GoogleServiceJwkVault jwkVault, String audience) {
        this.jwkVault = jwkVault;
        this.audience = audience;
    }

    public JWTVerifier resolve(String jwt) {
        RSAPublicKey publicKey = (RSAPublicKey) jwkVault.getPublicKey(jwt);
        return JWT.require(Algorithm.RSA256(publicKey, null)).build();
    }
}
