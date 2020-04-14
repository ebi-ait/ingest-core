package org.humancellatlas.ingest.security.authn.oidc;

import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class OpenIdAuthenticationTest {

    private final String subject = "73985cc";
    private final UserInfo userInfo = new UserInfo(subject);

    private Account account;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        account = new Account(subject);
        authentication = new OpenIdAuthentication(account, userInfo);
    }

    @Nested
    @DisplayName("Authentication")
    class AuthenticationTest {

        private OpenIdAuthentication authentication;

        @BeforeEach
        void setUp() {
            authentication = new OpenIdAuthentication(account);
        }

        @Test
        public void successful() {
            //when:
            authentication.authenticateWith(userInfo);

            //expect:
            assertThat(authentication.isAuthenticated()).isTrue();
        }

        @Test
        public void nonMatchingSubject() {
            //given:
            String anotherSubject = "82909a1";
            UserInfo anotherUserInfo = new UserInfo(anotherSubject);
            assumeThat(anotherSubject).isNotEqualTo(subject);

            //when:
            authentication.authenticateWith(anotherUserInfo);

            //then:
            assertThat(authentication.isAuthenticated()).isFalse();
        }

    }

    @Test
    public void testGetPrincipal() {
        //expect:
        assertThat(authentication.getPrincipal()).isEqualTo(account);
    }

    @Test
    public void testGetCredentials() {
        //expect:
        assertThat(authentication.getCredentials()).isEqualTo(userInfo);
    }

    @Test
    public void testGetAuthorities() {
        //given:
        account.addRole(Role.CONTRIBUTOR);

        //expect:
        //this assignment is to work around the weirdness with generic type that I couldn't figure out
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
        assertThat(authorities).containsExactly(Role.CONTRIBUTOR);
    }

    @Test
    public void testGetName() {
        //expect:
        assertThat(authentication.getName()).isEqualTo(subject);
    }

    @Test
    public void testGetDetails() {
        //expect:
        assertThat(authentication.getDetails()).isEqualTo(userInfo);
    }

}
