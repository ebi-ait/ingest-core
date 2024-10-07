package uk.ac.ebi.subs.ingest.security.authn.oidc;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import uk.ac.ebi.subs.ingest.security.Account;

public class OpenIdAuthentication implements Authentication {

  private Account account;
  private UserInfo userInfo;

  private boolean authenticated = false;

  public OpenIdAuthentication(final Account principal) {
    account = Account.GUEST;
    if (principal != null) {
      account = principal;
    }
  }

  public OpenIdAuthentication(Account principal, UserInfo credentials) {
    this(principal);
    authenticateWith(credentials);
  }

  public OpenIdAuthentication(UserInfo credentials) {
    this(null, credentials);
  }

  @Override
  public Object getPrincipal() {
    return account;
  }

  @Override
  public Object getCredentials() {
    return userInfo;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return account.getRoles();
  }

  @Override
  public String getName() {
    return account.getProviderReference();
  }

  @Override
  public Object getDetails() {
    return userInfo;
  }

  @Override
  public boolean isAuthenticated() {
    return authenticated;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    throw new IllegalArgumentException(
        "Operation not supported. Use authenticateWith to set status.");
  }

  public void authenticateWith(UserInfo credentials) {
    this.userInfo = credentials;
    if (credentials == null) {
      authenticated = false;
      return;
    }
    authenticated =
        account == Account.GUEST
            || account == Account.SERVICE
            || credentials.getSubjectId().equalsIgnoreCase(account.getProviderReference());
  }
}
