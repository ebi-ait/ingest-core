package org.humancellatlas.ingest.security;

import java.util.*;

public class Account {

    private final Set<Role> roles = new HashSet<>();

    public Account() {}

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

}
