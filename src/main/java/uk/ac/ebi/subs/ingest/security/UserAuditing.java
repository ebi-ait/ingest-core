package uk.ac.ebi.subs.ingest.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

    Object principal = authentication.getPrincipal();

    if (Account.class.isAssignableFrom(principal.getClass())) {
      Account account = (Account) authentication.getPrincipal();
      return Optional.of(account.getId() != null ? account.getId() : account.getName());
    } else {
      return Optional.ofNullable(principal.toString());
    }
  }
}
