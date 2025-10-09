package org.humancellatlas.ingest.security.common.jwk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration class to set up the static reference to JwtVerificationService
 * in DelegatingJwtAuthentication.
 */
@Configuration
public class JwtVerificationConfig {

    @Autowired
    private JwtVerificationService jwtVerificationService;

    @PostConstruct
    public void init() {
        DelegatingJwtAuthentication.setJwtVerificationService(jwtVerificationService);
    }
}
