package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for JWT verification with caching support.
 * This service provides cached JWT verification to improve performance
 * by avoiding repeated verification of the same tokens.
 */
@Service
public class JwtVerificationService {

    /**
     * Verifies a JWT token with caching support.
     * The verification result is cached using the token as the key.
     * 
     * @param token The JWT token string to verify
     * @param verifier The JWT verifier to use
     * @return A DecodedJWT instance with verified token
     */
    @Cacheable(value = "jwtVerification", key = "#token")
    public DecodedJWT verify(String token, JWTVerifier verifier) {
        return verifier.verify(token);
    }
}
