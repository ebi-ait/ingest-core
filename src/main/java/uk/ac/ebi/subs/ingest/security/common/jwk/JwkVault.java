package uk.ac.ebi.subs.ingest.security.common.jwk;

import java.security.PublicKey;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwkVault {

  PublicKey getPublicKey(DecodedJWT jwt);
}
