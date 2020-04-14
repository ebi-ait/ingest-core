package org.humancellatlas.ingest.security.authn.oidc;

import org.humancellatlas.ingest.security.Account;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenIdAuthenticationTest {

    @Test
    public void testGetPrincipal() {
        //given:
        Account account = new Account("8deeda9");
        Authentication authentication = new OpenIdAuthentication(account);

        //expect:
        assertThat(authentication.getPrincipal()).isEqualTo(account);
    }

}
