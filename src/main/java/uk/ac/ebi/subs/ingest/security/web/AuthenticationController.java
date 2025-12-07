package uk.ac.ebi.subs.ingest.security.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.authn.oidc.OpenIdAuthentication;

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
    var openIdAuthentication = (OpenIdAuthentication) authentication;
    Account account = (Account) openIdAuthentication.getPrincipal();
    return ResponseEntity.ok(account);
  }

  @GetMapping(path = "/account", produces = APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Account> getAccount(Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    return ResponseEntity.ok().body(account);
  }
}
