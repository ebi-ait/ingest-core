package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.humancellatlas.ingest.security.common.jwk.RemoteServiceJwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.humancellatlas.ingest.security.common.jwk.DelegatingJwtAuthentication;

public class ElixirAaiAuthenticationProvider implements AuthenticationProvider {
    private static Logger logger = LoggerFactory.getLogger(ElixirAaiAuthenticationProvider.class);

    private final RemoteServiceJwtVerifierResolver jwtVerifierResolver;

    public ElixirAaiAuthenticationProvider(RemoteServiceJwtVerifierResolver jwtVerifierResolver) {
        this.jwtVerifierResolver = jwtVerifierResolver;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        try {
            JwtAuthentication jwt = (JwtAuthentication) authentication;
            verifyIssuer(jwt);

            JWTVerifier jwtVerifier = jwtVerifierResolver.resolve(jwt.getToken());
            Authentication jwtAuth = DelegatingJwtAuthentication.delegate(jwt, jwtVerifier);
            logger.info("Authenticated with jwt with scopes {}", jwtAuth.getAuthorities());

            return jwtAuth;
        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationFailed(e);
        }
    }

    private void verifyIssuer(JwtAuthentication jwt) {
        DecodedJWT token = JWT.decode(jwt.getToken());
        String issuer = token.getIssuer();

        if (!issuer.equals("https://login.elixir-czech.org/oidc/")) {
            throw new UnlistedJwtIssuer(issuer);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}