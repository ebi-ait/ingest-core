package org.humancellatlas.ingest.security.authn.oidc;

import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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

    @Test
    public void testGetCredentials() {
        //given:
        String subject = "73985cc";
        Account account = new Account(subject);
        UserInfo userInfo = new UserInfo(subject);
        Authentication authentication = new OpenIdAuthentication(account, userInfo);

        //expect:
        assertThat(authentication.getCredentials()).isEqualTo(userInfo);
    }

    @Test
    public void testGetAuthorities() {
        //given:
        Account account = new Account("b94033d");
        account.addRole(Role.CONTRIBUTOR);
        Authentication authentication = new OpenIdAuthentication(account);

        //expect:
        //this assignment is to work around the weirdness with generic type that I couldn't figure out
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
        assertThat(authorities).containsExactly(Role.CONTRIBUTOR);
    }

    @Test
    public void testGetDetails() {
        //given:
        String subject = "89b4b40";
        Account account = new Account(subject);
        UserInfo userInfo = new UserInfo(subject);
        Authentication authentication = new OpenIdAuthentication(account, userInfo);

        //expect:
        assertThat(authentication.getDetails()).isEqualTo(userInfo);
    }

}
