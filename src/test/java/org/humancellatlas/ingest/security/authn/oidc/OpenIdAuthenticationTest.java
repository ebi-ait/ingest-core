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

    private final String subjectId = "73985cc";
    private final String issuer = "https://iss.domain.tld/oidc";
    private final UserInfo userInfo = new UserInfo(subjectId, issuer);

    private Account account;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        account = new Account(subjectId);
        authentication = new OpenIdAuthentication(account);
        ((OpenIdAuthentication) authentication).authenticateWith(userInfo);
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
            assertThat(authentication.getCredentials()).isEqualTo(userInfo);
        }

        @Test
        public void noPrincipal() {
            //given:
            authentication = new OpenIdAuthentication(null);

            //when:
            authentication.authenticateWith(userInfo);

            //expect:
            assertThat(authentication.isAuthenticated()).isFalse();
            assertThat(authentication.getCredentials()).isEqualTo(userInfo);
        }

        @Test
        public void nonMatchingSubjectId() {
            //given:
            String anotherSubjectId = "82909a1";
            UserInfo anotherUserInfo = new UserInfo(anotherSubjectId, issuer);
            assumeThat(anotherSubjectId).isNotEqualTo(subjectId);

            //when:
            authentication.authenticateWith(anotherUserInfo);

            //then:
            assertThat(authentication.isAuthenticated()).isFalse();
            assertThat(authentication.getCredentials()).isEqualTo(anotherUserInfo);
        }

        @Test
        public void noIssuer() {
            //given:
            UserInfo anotherUserInfo = new UserInfo(subjectId, "");

            //when:
            authentication.authenticateWith(anotherUserInfo);

            //then:
            assertThat(authentication.isAuthenticated()).isFalse();
            assertThat(authentication.getCredentials()).isEqualTo(anotherUserInfo);
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
        assertThat(authentication.getName()).isEqualTo(subjectId);
    }

    @Test
    public void testGetDetails() {
        //expect:
        assertThat(authentication.getDetails()).isEqualTo(userInfo);
    }

    @Test
    public void ensureNonNullPrincipal() {
        //expect:
        Authentication authentication  = new OpenIdAuthentication(null);
        assertThat(authentication.getPrincipal()).isSameAs(Account.GUEST);
    }

}
