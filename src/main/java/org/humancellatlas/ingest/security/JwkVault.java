package org.humancellatlas.ingest.security;

import java.security.PublicKey;

public interface JwkVault {

    PublicKey getPublicKey(String jwt);

}
