package uk.ac.ebi.subs.ingest.security.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.Role;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

  private final uk.ac.ebi.subs.ingest.security.AccountService accountService;

  public AuthenticationController(uk.ac.ebi.subs.ingest.security.AccountService accountService) {
    this.accountService = accountService;
  }

  /**
   * Registration endpoint: - No longer creates or updates accounts. - Just returns the currently
   * authenticated account. - New users are already created as GUEST by the
   * GlobusAuthenticationProvider. - This makes the call idempotent and avoids 409 conflicts.
   */
  @PostMapping(path = "/registration", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Account> register(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      // Filter chain already enforces "authenticated", but be defensive.
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Account)) {
      // Someone authenticated in a different way (e.g. UsernamePasswordAuthenticationToken in
      // tests)
      // → this endpoint is only meant for OIDC/Globus-backed Account principals.
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Account account = (Account) principal;
    return ResponseEntity.ok(account);
  }

  @GetMapping(path = "/account", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Account> getAccount(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return ResponseEntity.notFound().build();
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Account)) {
      return ResponseEntity.notFound().build();
    }

    Account account = (Account) principal;

    boolean guestOnly =
        account.getRoles() == null
            || account.getRoles().isEmpty()
            || account.getRoles().stream().allMatch(role -> role == Role.GUEST);

    boolean hasRealId = account.getId() != null && !account.getId().isBlank();

    // Guest with no persisted account → behave as "no account"
    if (guestOnly && !hasRealId) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(account);
  }
}
