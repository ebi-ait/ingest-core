package org.humancellatlas.ingest.security.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.security.core.AuthenticationException;

public class JwtVerificationFailed extends AuthenticationException {

    public JwtVerificationFailed(JWTVerificationException cause) {
        super("JWT verification failed.", cause);
    }

}
