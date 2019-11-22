package org.humancellatlas.ingest.security.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidUserGroup extends AuthenticationException {

    public InvalidUserGroup(String group) {
        super(String.format("Invalid user group, %s", group));
    }

}
