package org.humancellatlas.ingest.security;

import org.springframework.data.annotation.Id;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Account {

    @Id
    private String id;

    private final String providerReference;

    private final Set<Role> roles = new HashSet<>();

    public Account(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getId() {
        return id;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

}
