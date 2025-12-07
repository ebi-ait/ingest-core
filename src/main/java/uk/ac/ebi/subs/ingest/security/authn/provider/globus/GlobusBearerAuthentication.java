package uk.ac.ebi.subs.ingest.security.authn.provider.globus;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Just wraps a raw bearer token string so our GlobusAuthenticationProvider can get access to it.
 */
public class GlobusBearerAuthentication extends AbstractAuthenticationToken {

  private final String token;

  public GlobusBearerAuthentication(String token) {
    super(null); // no authorities yet
    this.token = token;
    setAuthenticated(false);
  }

  public GlobusBearerAuthentication(
      String token, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.token = token;
    setAuthenticated(true);
  }

  public String getToken() {
    return token;
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getPrincipal() {
    // We’ll resolve the principal in the provider based on /userinfo
    return null;
  }
}
