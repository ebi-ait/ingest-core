package uk.ac.ebi.subs.ingest.security.authn.provider.gcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.authn.oidc.OpenIdAuthentication;
import uk.ac.ebi.subs.ingest.security.authn.oidc.UserInfo;
import uk.ac.ebi.subs.ingest.security.common.jwk.DelegatingJwtAuthentication;
import uk.ac.ebi.subs.ingest.security.common.jwk.JwtVerifierResolver;
import uk.ac.ebi.subs.ingest.security.exception.JwtVerificationFailed;
import uk.ac.ebi.subs.ingest.security.exception.UnlistedJwtIssuer;

public class GoogleServiceJwtAuthenticationProvider implements AuthenticationProvider {

  private static Logger logger =
      LoggerFactory.getLogger(GoogleServiceJwtAuthenticationProvider.class);

  private final JwtVerifierResolver jwtVerifierResolver;

  private final GcpDomainWhiteList projectWhitelist;

  public GoogleServiceJwtAuthenticationProvider(
      GcpDomainWhiteList projectWhitelist, JwtVerifierResolver jwtVerifierResolver) {
    this.jwtVerifierResolver = jwtVerifierResolver;
    this.projectWhitelist = projectWhitelist;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthentication.class.isAssignableFrom(authentication);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!supports(authentication.getClass())) {
      return null;
    }
    try {
      JwtAuthentication jwt = (JwtAuthentication) authentication;
      verifyIssuer(jwt);

      JWTVerifier jwtVerifier = jwtVerifierResolver.resolve(jwt.getToken());
      Authentication jwtAuth = DelegatingJwtAuthentication.delegate(jwt, jwtVerifier);

      logger.info("Authenticated with jwt with scopes {}", jwtAuth.getAuthorities());

      Account account = Account.SERVICE;
      UserInfo serviceUser = new UserInfo(JWT.decode(jwt.getToken()).getSubject(), "password");
      OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account, serviceUser);
      return openIdAuth;
    } catch (JWTVerificationException e) {
      logger.error("JWT verification failed: {}", e.getMessage());
      throw new JwtVerificationFailed(e);
    }
  }

  private void verifyIssuer(JwtAuthentication jwt) {
    DecodedJWT token = JWT.decode(jwt.getToken());
    String issuer = token.getIssuer();

    if (!projectWhitelist.lists(issuer)) {
      throw UnlistedJwtIssuer.notWhitelisted(issuer);
    }
  }
}
