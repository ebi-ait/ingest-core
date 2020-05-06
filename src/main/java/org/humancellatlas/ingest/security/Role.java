package org.humancellatlas.ingest.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    CONTRIBUTOR, GUEST;

    @Override
    public String getAuthority() {
        return name();
    }

}
