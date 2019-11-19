package org.humancellatlas.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

public class UnlistedEmail extends AuthenticationException {

    public UnlistedEmail(String email) {
        super(String.format("User email %s is not in the whitelist.", email));
    }

}
