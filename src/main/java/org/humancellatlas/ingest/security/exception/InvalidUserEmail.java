package org.humancellatlas.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

import static java.lang.String.format;

public class InvalidUserEmail extends AuthenticationException {

    private final String email;

    public InvalidUserEmail(String email) {
        super(format("Email [%s] is not from valid domain.", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
