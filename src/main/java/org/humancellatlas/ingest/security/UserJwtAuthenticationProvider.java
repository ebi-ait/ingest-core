package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.humancellatlas.ingest.security.exception.InvalidUserGroup;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Nullable;

import static java.util.Optional.ofNullable;

public class UserJwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtAuthenticationProvider delegate;

    public UserJwtAuthenticationProvider(JwtAuthenticationProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication jwtAuthentication = delegate.authenticate(authentication);
        ofNullable(jwtAuthentication).ifPresent(auth -> {
            JwtAuthentication jwt = (JwtAuthentication) authentication;
            UserJwt user = new UserJwt(jwt);
            verifyUser(user);
        });
        return jwtAuthentication;
    }

    private void verifyUser(UserJwt user) {
        String group = user.getGroup();
        if (group == null || !group.toLowerCase().equals("hca")) {
            throw new InvalidUserGroup(group);
        }
    }

    @Override
    public boolean supports(@Nullable Class<?> authentication) {
        return delegate.supports(authentication);
    }

}
