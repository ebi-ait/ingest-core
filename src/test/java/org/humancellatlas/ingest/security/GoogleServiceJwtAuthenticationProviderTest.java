package org.humancellatlas.ingest.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GoogleServiceJwtAuthenticationProviderTest {

    @Nested
    @DisplayName("Authenticate")
    class AuthenticationTests {

        private static final String ISSUER = "https://humancellatlas.auth0.com";

        private JwtGenerator jwtGenerator = new JwtGenerator(ISSUER);

        private JWTVerifier jwtVerifier;

        private RemoteServiceJwtVerifierResolver jwtVerifierResolver;


        @BeforeEach
        public void setUp() {
            jwtVerifier = mock(JWTVerifier.class);
            jwtVerifierResolver = mock(RemoteServiceJwtVerifierResolver.class);
            doReturn(jwtVerifier).when(jwtVerifierResolver).resolve(anyString());
        }

        @Test
        @DisplayName("Success")
        public void testAuthenticate() {
            //given: JWT
            Map<String, String> claims = Map.ofEntries(
                    entry("https://auth.data.humancellatlas.org/email", "sample@domain.tld")
            );
            String keyId = "MDc2OTM3ODI4ODY2NUU5REVGRDVEM0MyOEYwQTkzNDZDRDlEQzNBRQ";
            String subject = "johndoe@somedomain.tld";
            String jwt = jwtGenerator.generate(keyId, subject, claims);

            //and: given a JWT Authentication
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            assumeThat(jwtAuthentication).isNotNull();

            //and:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    "https://dev.data.humancellatlas.org/", asList("auth0.com"), jwtVerifierResolver);

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).isNotNull()
                    .extracting("principal")
                    .containsExactly(subject);

        }

        @Test
        @DisplayName("unlisted issuer")
        public void testForUnlistedIssuer() {
            //given:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    "https://dev.data.humancellatlas.org/", asList("differentissuer.com"), jwtVerifierResolver);

            //and:
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(UnlistedJwtIssuer.class).hasMessageContaining(ISSUER);
        }

        @Test
        @DisplayName("verification failed")
        public void testForFailedVerification() {
            //given:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    "https://dev.data.humancellatlas.org/", asList("auth0.com"), jwtVerifierResolver);

            //and:
            Exception verificationFailed = new JWTVerificationException("verification failed");
            doThrow(verificationFailed).when(jwtVerifier).verify(anyString());

            //and:
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(JwtVerificationFailed.class);
        }

    }

}
