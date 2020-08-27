package org.humancellatlas.ingest.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    WRANGLER, CONTRIBUTOR, GUEST, SERVICE;

    @Override
    public String getAuthority() {
        return name();
    }

}
