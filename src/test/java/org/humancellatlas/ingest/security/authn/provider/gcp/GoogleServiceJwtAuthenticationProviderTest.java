package org.humancellatlas.ingest.security.authn.provider.gcp;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.humancellatlas.ingest.security.JwtGenerator;
import org.humancellatlas.ingest.security.authn.provider.gcp.GcpDomainWhiteList;
import org.humancellatlas.ingest.security.authn.provider.gcp.GoogleServiceJwtAuthenticationProvider;
import org.humancellatlas.ingest.security.common.jwk.RemoteServiceJwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.util.Map;

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
        private JWTVerifier jwtVerifier;

        private RemoteServiceJwtVerifierResolver jwtVerifierResolver;

        private GcpDomainWhiteList projectWhitelist;

        @BeforeEach
        public void setUp() {
            jwtVerifier = mock(JWTVerifier.class);
            jwtVerifierResolver = mock(RemoteServiceJwtVerifierResolver.class);
            doReturn(jwtVerifier).when(jwtVerifierResolver).resolve(anyString());

            projectWhitelist = mock(GcpDomainWhiteList.class);
            doReturn(true).when(projectWhitelist).lists("sample@domain.tld");
        }

        @Test
        @DisplayName("success")
        public void testAuthenticate() {
            //given: JWT
            Map<String, String> claims = Map.ofEntries(
                    entry("https://auth.data.humancellatlas.org/group", "public")
            );
            String keyId = "MDc2OTM3ODI4ODY2NUU5REVGRDVEM0MyOEYwQTkzNDZDRDlEQzNBRQ";
            String subject = "johndoe@somedomain.tld";

            JwtGenerator jwtGenerator = new JwtGenerator("sample@domain.tld");
            String jwt = jwtGenerator.generate(keyId, subject, claims);

            //and: given a JWT Authentication
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            assumeThat(jwtAuthentication).isNotNull();

            //and:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    projectWhitelist, jwtVerifierResolver);

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).isNotNull();
        }

        @Test
        @DisplayName("unlisted issuer")
        public void testForUnlistedIssuer() {
            //given:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    projectWhitelist, jwtVerifierResolver);

            //and:
            JwtGenerator jwtGenerator = new JwtGenerator("sample@otherdomain.tld");
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(UnlistedJwtIssuer.class).hasMessageContaining("sample@otherdomain.tld");
        }

        @Test
        @DisplayName("verification failed")
        public void testForFailedVerification() {
            //given:
            AuthenticationProvider authenticationProvider = new GoogleServiceJwtAuthenticationProvider(
                    projectWhitelist, jwtVerifierResolver);

            //and:
            Exception verificationFailed = new JWTVerificationException("verification failed");
            doThrow(verificationFailed).when(jwtVerifier).verify(anyString());

            //and:
            JwtGenerator jwtGenerator = new JwtGenerator("sample@domain.tld");
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(JwtVerificationFailed.class);
        }

    }

}
