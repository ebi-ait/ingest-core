package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedEmail;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.humancellatlas.ingest.security.spring.DelegatingJwtAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

public class GoogleServiceJwtAuthenticationProvider implements AuthenticationProvider {

    private static Logger logger = LoggerFactory.getLogger(GoogleServiceJwtAuthenticationProvider.class);

    private final RemoteServiceJwtVerifierResolver jwtVerifierResolver;

    private final UserWhiteList userWhiteList;

    private final List<String> projects;

    private final String audience;

    public GoogleServiceJwtAuthenticationProvider(String audience, List<String> projects,
            RemoteServiceJwtVerifierResolver jwtVerifierResolver, UserWhiteList userWhiteList) {
        this.audience = audience;
        this.projects = projects;
        this.jwtVerifierResolver = jwtVerifierResolver;
        this.userWhiteList = userWhiteList;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
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

            verifyPrincipal(jwtAuth);
            return jwtAuth;
        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationFailed(e);
        }
    }

    private void verifyIssuer(JwtAuthentication jwt) {
        DecodedJWT token = JWT.decode(jwt.getToken());
        String issuer = token.getIssuer();
        boolean match = projects.stream().anyMatch(issuer::endsWith);
        if (!match) {
            throw new UnlistedJwtIssuer(issuer);
        }
    }

    private void verifyPrincipal(Authentication jwtAuth) {
        String principal = jwtAuth.getPrincipal().toString();
        if (!userWhiteList.lists(principal)) {
            throw new UnlistedEmail(principal);
        }
    }

}
