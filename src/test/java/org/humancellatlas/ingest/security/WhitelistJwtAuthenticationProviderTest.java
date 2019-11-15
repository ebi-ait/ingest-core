package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class WhitelistJwtAuthenticationProviderTest {

    @Nested
    @DisplayName("authentication")
    class AuthenticationTest {

        @Test
        @DisplayName("authentication succeeds")
        public void testAuthenticate() {
            //given: JWT authentication
            JwtGenerator jwtGenerator = new JwtGenerator();
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
            assertThat(authentication).isNotNull();
        }

    }

}
