package org.humancellatlas.ingest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@Component
public class UserWhiteList {

    private final List<String> domains;

    @Autowired
    public UserWhiteList(@Value("#{'${SECURITY_WHITELIST_DOMAINS}'.split(',')}") List<String> domains) {
        this.domains = Collections.unmodifiableList(domains);
    }

    public UserWhiteList(String... domains) {
        this(asList(domains));
    }

    public boolean lists(String email) {
        return domains.stream()
                .map(domain -> format("@%s", domain))
                .anyMatch(email::endsWith);
    }

}
