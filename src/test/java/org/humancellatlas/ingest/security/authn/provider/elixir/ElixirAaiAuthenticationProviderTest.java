package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.humancellatlas.ingest.security.JwtGenerator;
import org.humancellatlas.ingest.security.common.jwk.RemoteServiceJwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.InvalidUserEmail;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ElixirAaiAuthenticationProviderTest {
    private static MockWebServer mockBackEnd;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Nested
    @DisplayName("Authenticate")
    class AuthenticationTests {

        private WebTestClient webTestClient;

        private JWTVerifier jwtVerifier;

        private RemoteServiceJwtVerifierResolver jwtVerifierResolver;

        @BeforeEach
        public void setUp() {
            jwtVerifier = mock(JWTVerifier.class);
            jwtVerifierResolver = mock(RemoteServiceJwtVerifierResolver.class);
            doReturn(jwtVerifier).when(jwtVerifierResolver).resolve(anyString());
            String baseUrl = String.format("http://localhost:%s",
                    mockBackEnd.getPort());
            doReturn(baseUrl).when(jwtVerifierResolver).getIssuer();
        }

        @Test
        @DisplayName("success")
        public void testAuthenticate() throws JsonProcessingException {
            //given: JWT
            String keyId = "MDc2OTM3ODI4ODY2NUU5REVGRDVEM0MyOEYwQTkzNDZDRDlEQzNBRQ";
            String subject = "johndoe@elixirdomain.tld";

            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.generate(keyId, subject, null);

            //and: given a JWT Authentication
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            assumeThat(jwtAuthentication).isNotNull();

            //and
            ElixirUserInfo mockUserInfo = new ElixirUserInfo("sub", "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(new ObjectMapper().writeValueAsString(mockUserInfo))
                    .addHeader("Content-Type", "application/json"));

            //and:
            AuthenticationProvider authenticationProvider = new ElixirAaiAuthenticationProvider(jwtVerifierResolver);

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).isNotNull();
        }

        @Test
        @DisplayName("invalid user email")
        public void testForInvalidUserEmail() throws JsonProcessingException {
            //given:
            AuthenticationProvider authenticationProvider = new ElixirAaiAuthenticationProvider(jwtVerifierResolver);

            //and:
            ElixirUserInfo mockUserInfo = new ElixirUserInfo("sub", "name", "pref", "giv", "fam", "email@embl.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(new ObjectMapper().writeValueAsString(mockUserInfo))
                    .addHeader("Content-Type", "application/json"));

            //and:
            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(InvalidUserEmail.class).hasMessageContaining("email@embl.ac.uk");
        }

        @Test
        @DisplayName("valid user email")
        public void testForValidUserEmail() throws JsonProcessingException {
            //given:
            AuthenticationProvider authenticationProvider = new ElixirAaiAuthenticationProvider(jwtVerifierResolver);

            //and:
            ElixirUserInfo mockUserInfo = new ElixirUserInfo("sub", "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(new ObjectMapper().writeValueAsString(mockUserInfo))
                    .addHeader("Content-Type", "application/json"));

            //and:
            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.generate();
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            Authentication auth = authenticationProvider.authenticate(jwtAuthentication);
            //expect:
            //then:
            assertThat(auth).isNotNull();
        }

        @Test
        @DisplayName("verification failed")
        public void testForFailedVerification() throws JsonProcessingException {
            //given:
            AuthenticationProvider authenticationProvider = new ElixirAaiAuthenticationProvider(jwtVerifierResolver);

            //and:
            ElixirUserInfo mockUserInfo = new ElixirUserInfo("sub", "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(new ObjectMapper().writeValueAsString(mockUserInfo))
                    .addHeader("Content-Type", "application/json"));

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
