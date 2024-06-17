package org.humancellatlas.ingest.security.common.jwk;

import org.springframework.security.core.Authentication;

/**
 * A custom Authentication interface based on Auth0's JwtAuthentication without the frustrating
 * verify method that takes concrete JWTVerifier instead of the JWTVerifier interface (yeah,
 * confusing).
 */
public interface Auth0JwtAuthentication extends Authentication {

  String getToken();

  String getKeyId();
}
