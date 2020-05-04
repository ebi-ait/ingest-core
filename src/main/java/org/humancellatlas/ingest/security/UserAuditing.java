package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
* User auditing to get the current user to put into the database: Based on https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing
 */

@Component
public class UserAuditing implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (OpenIdAuthentication.class.isAssignableFrom(authentication.getClass())) {
            Account account = (Account) authentication.getPrincipal();
            return Optional.of(account.getId());
        } else {
            return Optional.ofNullable(authentication.getPrincipal().toString());
        }
    }

}