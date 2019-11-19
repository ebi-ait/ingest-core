package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.humancellatlas.ingest.security.exception.UnlistedEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class WhitelistJwtAuthenticationProviderTest {

    @Nested
    @DisplayName("authentication")
    class AuthenticationTest {

        private JwtGenerator jwtGenerator = new JwtGenerator();

        @Test
        @DisplayName("authentication succeeds")
        public void testAuthenticate() {
            //given: JWT authentication
            String userEmail = "trustedfellow@friendlysite.com";
            String jwt = jwtGenerator.generateWithSubject(userEmail);
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and:
            JwtAuthenticationProvider delegate = mock(JwtAuthenticationProvider.class);
            doReturn(jwtAuthentication).when(delegate).authenticate(any(Authentication.class));

            //and:
            UserWhiteList userWhitelist = mock(UserWhiteList.class);
            doReturn(true).when(userWhitelist).lists(anyString());

            //and:
            AuthenticationProvider authenticationProvider = new WhitelistJwtAuthenticationProvider(delegate,
                    userWhitelist);

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).extracting("principal").containsExactly(userEmail);
        }

        @Test
        @DisplayName("unlisted emails")
        public void testForUnlistedEmail() {
            //given: JWT authentication
            String userEmail = "suspicious@unknown.net";
            String jwt = jwtGenerator.generateWithSubject(userEmail);
            Authentication authentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and:
            JwtAuthenticationProvider delegate = mock(JwtAuthenticationProvider.class);
            doReturn(authentication).when(delegate).authenticate(any(Authentication.class));

            //and:
            UserWhiteList userWhitelist = mock(UserWhiteList.class);
            doReturn(false).when(userWhitelist).lists(anyString());

            //and:
            AuthenticationProvider authenticationProvider = new WhitelistJwtAuthenticationProvider(delegate,
                    userWhitelist);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(authentication);
            }).isExactlyInstanceOf(UnlistedEmail.class).hasMessageContaining(userEmail);
        }

    }

}
