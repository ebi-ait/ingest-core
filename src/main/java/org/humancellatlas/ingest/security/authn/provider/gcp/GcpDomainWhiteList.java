package org.humancellatlas.ingest.security.authn.provider.gcp;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;


public class GcpDomainWhiteList {

    private final List<String> domains;

    public GcpDomainWhiteList(List<String> domains) {
        this.domains = Collections.unmodifiableList(domains);
    }

    public GcpDomainWhiteList(String... domains) {
        this(asList(domains));
    }

    public boolean lists(String email) {
        return domains.stream()
                .map(domain -> format("@%s", domain))
                .anyMatch(email::endsWith);
    }

}
