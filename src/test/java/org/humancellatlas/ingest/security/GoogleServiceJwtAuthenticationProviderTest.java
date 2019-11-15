package org.humancellatlas.ingest.security;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.nio.file.Path;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GoogleServiceJwtAuthenticationProviderTest {

    private static final Path RESOURCE_PATH = Path.of("src/test/resources");

    @Nested
    @DisplayName("Authenticate")
    class AuthenticationTests {

        @Test
        @DisplayName("Authentication succeeds")
        public void testAuthenticate() {
            //given: JWT
            String issuer = "https://humancellatlas.auth0.com";
            JwtGenerator jwtGenerator = new JwtGenerator(issuer);
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
            JWTVerifier jwtVerifier = mock(JWTVerifier.class);
            RemoteServiceJwtVerifierResolver jwtVerifierResolver = mock(RemoteServiceJwtVerifierResolver.class);
            doReturn(jwtVerifier).when(jwtVerifierResolver).resolve(anyString());

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

    }

}
