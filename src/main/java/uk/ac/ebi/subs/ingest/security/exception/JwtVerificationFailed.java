package uk.ac.ebi.subs.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class JwtVerificationFailed extends AuthenticationException {

  public JwtVerificationFailed(JWTVerificationException cause) {
    super("JWT verification failed.", cause);
  }
}
