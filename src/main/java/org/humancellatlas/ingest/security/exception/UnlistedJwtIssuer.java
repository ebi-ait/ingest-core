package org.humancellatlas.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

import static java.lang.String.format;

public class UnlistedJwtIssuer extends AuthenticationException {

    private final String issuer;

    public UnlistedJwtIssuer(String issuer) {
        super(format("Issuer [%s] is not specified in the whitelist.", issuer));
        this.issuer = issuer;
    }

    public String getIssuer() {
        return issuer;
    }

}
