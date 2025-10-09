package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class DelegatingJwtAuthentication implements Auth0JwtAuthentication {

    private static JwtVerificationService jwtVerificationService;

    private Authentication authentication;

    private DecodedJWT token;

    public static DelegatingJwtAuthentication delegate(JwtAuthentication source, JWTVerifier verifier) {
        var authentication = source.verify(null);
        DecodedJWT token = verifier.verify(source.getToken());
        return new DelegatingJwtAuthentication(authentication, token);
    }

    /**
     * Creates a DelegatingJwtAuthentication with cached JWT verification.
     * This method uses the JwtVerificationService to cache verification results.
     * 
     * @param source The JWT authentication source
     * @param verifier The JWT verifier to use
     * @return A DelegatingJwtAuthentication instance with verified token
     */
    public static DelegatingJwtAuthentication delegateWithCache(JwtAuthentication source, JWTVerifier verifier) {
        if (jwtVerificationService == null) {
            // Fallback to non-cached verification if service is not available
            return delegate(source, verifier);
        }
        var authentication = source.verify(null);
        DecodedJWT token = jwtVerificationService.verify(source.getToken(), verifier);
        return new DelegatingJwtAuthentication(authentication, token);
    }

    public static void setJwtVerificationService(JwtVerificationService jwtVerificationService) {
        DelegatingJwtAuthentication.jwtVerificationService = jwtVerificationService;
    }

    DelegatingJwtAuthentication(Authentication authentication, DecodedJWT token) {
        this.authentication = authentication;
        this.token = token;
    }

    @Override
    public String getToken() {
        return token.getToken();
    }

    @Override
    public String getKeyId() {
        return token.getKeyId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authentication.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        //The construction of this object would only succeed if the token has first
        //been successfully verified.
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Authenticate through delegation to a new instance.");
    }

    @Override
    public String getName() {
        return authentication.getName();
    }

}
