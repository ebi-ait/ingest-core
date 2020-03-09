package org.humancellatlas.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

import static java.lang.String.format;

public class UnlistedJwtIssuer extends AuthenticationException {

    private final String issuer;

    public UnlistedJwtIssuer(String issuer, String message) {
        super(message);
        this.issuer = issuer;
    }

    public static UnlistedJwtIssuer notWhitelisted(String issuer) {
        return new UnlistedJwtIssuer(format("Issuer [%s] is not specified in the whitelist.", issuer), issuer);
    }

    public String getIssuer() {
        return issuer;
    }

}
