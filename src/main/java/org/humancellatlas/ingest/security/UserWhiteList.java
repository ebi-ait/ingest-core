package org.humancellatlas.ingest.security;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class UserWhiteList {

    private final List<String> domains;

    public UserWhiteList(String... domains) {
        this.domains = asList(domains);
    }

    public boolean lists(String email) {
        return domains.stream()
                .map(domain -> format("@%s", domain))
                .anyMatch(email::endsWith);
    }

}
