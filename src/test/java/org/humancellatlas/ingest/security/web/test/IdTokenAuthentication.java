package org.humancellatlas.ingest.security.web.test;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;

public class IdTokenAuthentication extends TestingAuthenticationToken implements JwtAuthentication {

    private IdToken idToken;

    public IdTokenAuthentication(IdToken idToken) {
        super(idToken.getSubject(), idToken, new ArrayList<>());
        this.idToken = idToken;
    }

    @Override
    public String getToken() {
        return idToken.toJwt();
    }

    @Override
    public String getKeyId() {
        return null;
    }

    @Override
    public Authentication verify(JWTVerifier verifier) throws JWTVerificationException {
        throw new UnsupportedOperationException("Verification is not supported in tests.");
    }

}
