package org.humancellatlas.ingest.security.common.jwk;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.PublicKey;

public interface JwkVault {

    PublicKey getPublicKey(DecodedJWT jwt);

}
