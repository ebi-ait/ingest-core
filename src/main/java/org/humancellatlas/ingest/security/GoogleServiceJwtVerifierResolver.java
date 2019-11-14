package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.humancellatlas.ingest.security.jwk.RemoteJwkVault;

import java.security.interfaces.RSAPublicKey;

public class GoogleServiceJwtVerifierResolver {

    private final RemoteJwkVault jwkVault;

    private final String audience;

    public GoogleServiceJwtVerifierResolver(RemoteJwkVault jwkVault, String audience) {
        this.jwkVault = jwkVault;
        this.audience = audience;
    }

    public JWTVerifier resolve(String jwt) {
        DecodedJWT token = JWT.decode(jwt);
        RSAPublicKey publicKey = (RSAPublicKey) jwkVault.getPublicKey(token);
        return JWT.require(Algorithm.RSA256(publicKey, null)).build();
    }
}
