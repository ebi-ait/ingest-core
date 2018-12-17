package org.humancellatlas.ingest.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

public class ServiceAuthenticationProvider implements AuthenticationProvider {

    private static Logger logger = LoggerFactory.getLogger(ServiceAuthenticationProvider.class);
    private final String audience;

    public ServiceAuthenticationProvider(String audience) {
        this.audience = audience;
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

        JwtAuthentication jwt = (JwtAuthentication) authentication;
        try {
            final Authentication jwtAuth = jwt.verify(jwtVerifier(jwt));
            logger.info("Authenticated with jwt with scopes {}", jwtAuth.getAuthorities());
            return jwtAuth;
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Not a valid token", e);
        }
    }

    private JWTVerifier jwtVerifier(JwtAuthentication authentication) {
        DecodedJWT jwt = JWT.decode(authentication.getToken());
        String issuer = jwt.getIssuer();
        JwkProvider urlJwkProvider;

        try {
            URL url = new URL("https://www.googleapis.com/service_accounts/v1/jwk/" + issuer);
            urlJwkProvider = new UrlJwkProvider(url);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }

        RSAPublicKey publicKey;
        try{
            Jwk jwk = urlJwkProvider.get(authentication.getKeyId());
            publicKey = (RSAPublicKey) jwk.getPublicKey();
        } catch (JwkException e) {
            throw new AuthenticationServiceException("Cannot authenticate with jwt", e);
        }


        return JWT.require(Algorithm.RSA256(publicKey))
                .withIssuer(issuer)
                .withAudience(this.audience)
                .build();
    }
}
