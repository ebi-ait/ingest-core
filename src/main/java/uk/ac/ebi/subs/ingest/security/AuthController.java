package uk.ac.ebi.subs.ingest.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles registration of the currently authenticated user.
 *
 * <p>Behaviour: - If the principal is already a persisted Account (id != null): → return it as-is
 * (Globus or already-registered Cognito user). - If the principal is an Account with id == null: →
 * persist it via AccountService.register(...) and return the saved instance.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AccountService accountService;

  @PostMapping("/registration")
  public Account register() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null) {
      log.error("[AuthController] No Authentication in SecurityContext");
      throw new IllegalStateException("No Authentication in SecurityContext");
    }

    Object principal = auth.getPrincipal();

    if (!(principal instanceof Account)) {
      String type = principal == null ? "null" : principal.getClass().getName();
      log.error("[AuthController] Expected principal of type Account but got {}", type);
      throw new IllegalStateException("Expected principal of type Account but got " + type);
    }

    Account current = (Account) principal;

    if (current.getProviderReference() == null || current.getProviderReference().isEmpty()) {
      log.error("[AuthController] Account has no providerReference");
      throw new IllegalStateException("Account has no providerReference");
    }

    // If already persisted (Globus flow or already-registered Cognito), just return it
    if (current.getId() != null && !current.getId().isEmpty()) {
      log.debug(
          "[AuthController] /auth/registration for existing account id={}, providerRef={}",
          current.getId(),
          current.getProviderReference());
      return current;
    }

    // Otherwise, persist this first-time user
    log.debug(
        "[AuthController] Registering new account for providerRef={}",
        current.getProviderReference());

    Account saved = accountService.register(current);

    log.info(
        "[AuthController] Registered new account id={}, providerRef={}",
        saved.getId(),
        saved.getProviderReference());

    // IMPORTANT: return the saved instance, not the original transient one
    return saved;
  }
}
