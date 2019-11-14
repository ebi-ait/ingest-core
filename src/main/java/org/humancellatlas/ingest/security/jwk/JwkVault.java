package org.humancellatlas.ingest.security.jwk;

import java.security.PublicKey;

public interface JwkVault {

    PublicKey getPublicKey(String jwt);

}
