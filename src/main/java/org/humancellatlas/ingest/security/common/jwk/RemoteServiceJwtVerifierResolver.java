package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Getter;

import java.security.interfaces.RSAPublicKey;

public class RemoteServiceJwtVerifierResolver {

    private final JwkVault jwkVault;

    private final String audience;

    @Getter
    private final String issuer;

    public RemoteServiceJwtVerifierResolver(JwkVault jwkVault, String audience, String issuer) {
        this.jwkVault = jwkVault;
        this.audience = audience;
        this.issuer = issuer;
    }

    public JWTVerifier resolve(String jwt) {

        DecodedJWT token = JWT.decode(jwt);
        RSAPublicKey publicKey = (RSAPublicKey) jwkVault.getPublicKey(token);
        DelegatingJwtVerifier.Builder builder = DelegatingJwtVerifier.require(Algorithm.RSA256(publicKey, null));
        if (audience != null) {
            builder = builder.withAudience(audience);
        }

        if (issuer != null) {
            builder.withIssuer(issuer);
        } else {
            // for GCP JWTs
            builder.withIssuer(token.getIssuer());
        }
        return builder.build();
    }

}