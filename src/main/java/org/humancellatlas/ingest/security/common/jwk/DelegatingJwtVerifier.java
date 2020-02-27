package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Verification;

public class DelegatingJwtVerifier implements JWTVerifier {

    public static class Builder {

        private Verification verification;

        private String audience;
        private String issuer;

        private Builder(Verification verification) {
            this.verification = verification;
        }

        public static Builder require(Algorithm algorithm) {
            return new Builder(JWT.require(algorithm));
        }

        public Builder withAudience(String audience) {
            this.audience = audience;
            verification.withAudience(audience);
            return this;
        }

        public Builder withIssuer(String issuer) {
            this.issuer = issuer;
            verification.withIssuer(issuer);
            return this;
        }

        public JWTVerifier build() {
            DelegatingJwtVerifier verifier = new DelegatingJwtVerifier(verification.build());
            verifier.audience = audience;
            verifier.issuer = issuer;
            return verifier;
        }

    }

    private final JWTVerifier delegate;

    private String audience;
    private String issuer;

    /**
     * Effectively an alias for {@link Builder#require(Algorithm)}.
     */
    public static Builder require(Algorithm algorithm) {
        return Builder.require(algorithm);
    }

    private DelegatingJwtVerifier(JWTVerifier delegate) {
        this.delegate = delegate;
    }

    public String getAudience() {
        return audience;
    }

    public String getIssuer() {
        return issuer;
    }

    @Override
    public DecodedJWT verify(String token) throws JWTVerificationException {
        return delegate.verify(token);
    }

    @Override
    public DecodedJWT verify(DecodedJWT jwt) throws JWTVerificationException {
        return delegate.verify(jwt);
    }

}
