package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class DelegatingJwtAuthentication implements Auth0JwtAuthentication {

    private Authentication authentication;

    private DecodedJWT token;

    public static DelegatingJwtAuthentication delegate(JwtAuthentication source, JWTVerifier verifier) {
        var authentication = source.verify(null);
        DecodedJWT token = verifier.verify(source.getToken());
        return new DelegatingJwtAuthentication(authentication, token);
    }

    private DelegatingJwtAuthentication(Authentication authentication, DecodedJWT token) {
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
        // been successfully verified.
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
