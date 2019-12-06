package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.humancellatlas.ingest.security.exception.InvalidUserGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class UserJwtAuthenticationProviderTest {

    @Nested
    @DisplayName("authentication")
    class AuthenticationTest {
        private JwtGenerator jwtGenerator = new JwtGenerator();

        @Test
        @DisplayName("authentication succeeds")
        public void testAuthenticate() {
            //given: JWT authentication
            String userEmail = "trustedfellow@friendlysite.com";
            Map<String, String> claims = Map.ofEntries(
                    entry("https://auth.data.humancellatlas.org/group", "hca")
            );

            String jwt = jwtGenerator.generate(null, userEmail, claims);
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and:
            JwtAuthenticationProvider delegate = mock(JwtAuthenticationProvider.class);
            doReturn(jwtAuthentication).when(delegate).authenticate(any(Authentication.class));

            //and:
            DomainWhiteList userWhitelist = mock(DomainWhiteList.class);
            doReturn(true).when(userWhitelist).lists(anyString());

            //and:
            AuthenticationProvider authenticationProvider = new UserJwtAuthenticationProvider(delegate);

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).extracting("principal").containsExactly(userEmail);
        }

        @Test
        @DisplayName("no user group")
        public void testForNoUserGroup() {
            //given: JWT authentication
            String userGroup = "null";
            String jwt = jwtGenerator.generateWithSubject(userGroup);
            Authentication authentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and:
            JwtAuthenticationProvider delegate = mock(JwtAuthenticationProvider.class);
            doReturn(authentication).when(delegate).authenticate(any(Authentication.class));

            //and:
            DomainWhiteList userWhitelist = mock(DomainWhiteList.class);
            doReturn(false).when(userWhitelist).lists(anyString());

            //and:
            AuthenticationProvider authenticationProvider = new UserJwtAuthenticationProvider(delegate);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(authentication);
            }).isExactlyInstanceOf(InvalidUserGroup.class).hasMessageContaining(userGroup);
        }

        @Test
        @DisplayName("invalid user group")
        public void testForInvalidUserGroup() {
            //given: JWT authentication
            String userGroup = "public";
            Map<String, String> claims = Map.ofEntries(
                    entry("https://auth.data.humancellatlas.org/group", userGroup)
            );
            String jwt = jwtGenerator.generate(null, userGroup, claims);
            Authentication authentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and:
            JwtAuthenticationProvider delegate = mock(JwtAuthenticationProvider.class);
            doReturn(authentication).when(delegate).authenticate(any(Authentication.class));

            //and:
            DomainWhiteList userWhitelist = mock(DomainWhiteList.class);
            doReturn(false).when(userWhitelist).lists(anyString());

            //and:
            AuthenticationProvider authenticationProvider = new UserJwtAuthenticationProvider(delegate);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(authentication);
            }).isExactlyInstanceOf(InvalidUserGroup.class).hasMessageContaining(userGroup);
        }

    }

}
