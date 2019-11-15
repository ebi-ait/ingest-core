package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class WhitelistJwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtAuthenticationProvider delegate;

    private final UserWhiteList userWhitelist;

    public WhitelistJwtAuthenticationProvider(JwtAuthenticationProvider delegate, UserWhiteList userWhitelist) {
        this.delegate = delegate;
        this.userWhitelist = userWhitelist;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return delegate.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }

}
