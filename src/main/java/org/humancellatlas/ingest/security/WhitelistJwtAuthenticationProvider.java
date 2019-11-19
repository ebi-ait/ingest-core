package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.humancellatlas.ingest.security.exception.UnlistedEmail;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class WhitelistJwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtAuthenticationProvider delegate;

    private final UserWhiteList userWhitelist;

    public WhitelistJwtAuthenticationProvider(JwtAuthenticationProvider delegate, UserWhiteList userWhitelist) {
        this.delegate = delegate;
        this.userWhitelist = userWhitelist;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication jwtAuthentication = delegate.authenticate(authentication);
        ofNullable(jwtAuthentication).ifPresent(auth -> {
            String principal = auth.getPrincipal().toString();
            if (!userWhitelist.lists(principal)) {
                throw new UnlistedEmail(principal);
            }
        });
        return jwtAuthentication;
    }

    @Override
    public boolean supports(@Nullable Class<?> authentication) {
        return delegate.supports(authentication);
    }

}
