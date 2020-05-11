package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountRepository;
import org.humancellatlas.ingest.security.JwtGenerator;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.common.jwk.JwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.InvalidUserEmail;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes={ElixirAaiAuthenticationProviderTest.Config.class})
@AutoConfigureWebClient
public class ElixirAaiAuthenticationProviderTest {

    @Configuration
    @Import(ElixirAaiAuthenticationProvider.class)
    static class Config {}

    private MockWebServer mockBackEnd;

    @MockBean
    private JWTVerifier jwtVerifier;

    @MockBean
    @Qualifier(ELIXIR)
    private JwtVerifierResolver jwtVerifierResolver;

    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Nested
    @DisplayName("Authenticate")
    class AuthenticationTests {

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        public void setUp() throws Exception {
            mockBackEnd = new MockWebServer();
            mockBackEnd.start();

            doReturn(jwtVerifier).when(jwtVerifierResolver).resolve(anyString());
            String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
            doReturn(baseUrl).when(jwtVerifierResolver).getIssuer();
        }

        @AfterEach
        public void tearDown() throws Exception {
            mockBackEnd.shutdown();
        }

        @Test
        @DisplayName("success")
        public void testAuthenticate() throws Exception {
            //given: JWT
            String subject = "johndoe@elixirdomain.tld";
            UserInfo userInfo = new UserInfo(subject, "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.encode(userInfo);

            //and: given a JWT Authentication
            var jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            assumeThat(jwtAuthentication).isNotNull();

            //and: given JWT Verifier will verify token successfully
            DecodedJWT token = mock(DecodedJWT.class);
            doReturn(jwt).when(token).getToken();
            doReturn(token).when(jwtVerifier).verify(jwtAuthentication.getToken());

            //and: given account with same provider reference will be found
            Account account = new Account("73bbc45", subject);
            doReturn(account).when(accountRepository).findByProviderReference(subject);

            //and: Elixir user info will be returned
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userInfo))
                    .addHeader("Content-Type", "application/json"));

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).extracting("authenticated", "principal");
            assertThat(authentication.isAuthenticated()).isTrue();
            assertThat(authentication.getPrincipal()).isEqualTo(account);
            assertCorrectRequest(jwtAuthentication.getToken());
        }

        @Test
        @DisplayName("no account")
        public void testForNoAccount() throws Exception {
            //given: JWT
            String subject = "johndoe@elixirdomain.tld";
            UserInfo userInfo = new UserInfo(subject, "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            String jwt = new JwtGenerator("elixir").encode(userInfo);

            //and: given a JWT Authentication
            var jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
            assumeThat(jwtAuthentication).isNotNull();

            //and: given JWT Verifier will verify token successfully
            DecodedJWT token = mock(DecodedJWT.class);
            doReturn(jwt).when(token).getToken();
            doReturn(token).when(jwtVerifier).verify(jwtAuthentication.getToken());

            //and: Elixir user info will be returned
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userInfo))
                    .addHeader("Content-Type", "application/json"));

            //and: no matching records in the database
            doReturn(null).when(accountRepository).findByProviderReference(anyString());

            //when:
            Authentication authentication = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(Account.GUEST);
            assertCorrectRequest(jwtAuthentication.getToken());

            //and:
            assertThat(authentication.getCredentials()).isInstanceOf(UserInfo.class);
            UserInfo credentials = (UserInfo) authentication.getCredentials();
            assertThat(credentials).isEqualToComparingFieldByField(userInfo);
        }

        private void assertCorrectRequest(String token) throws InterruptedException {
            RecordedRequest request = mockBackEnd.takeRequest();
            assertThat(request.getMethod()).isEqualToIgnoringCase("GET");
            String bearerToken = String.format("Bearer %s", token);
            assertThat(request.getHeaders().get("Authorization")).isEqualTo(bearerToken);
        }

        @Test
        @DisplayName("invalid user email")
        public void testForInvalidUserEmail() throws JsonProcessingException {
            //given:
            String invalidEmail = "email@embl.ac.uk";
            UserInfo userInfo = new UserInfo("sub", "name", "pref", "giv", "fam", invalidEmail);
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userInfo))
                    .addHeader("Content-Type", "application/json"));

            //and:
            String jwt = new JwtGenerator("elixir").generate();
            doReturn(JWT.decode(jwt)).when(jwtVerifier).verify(anyString());
            Authentication jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(InvalidUserEmail.class).hasMessageContaining(invalidEmail);
        }

        @Test
        @DisplayName("valid user email")
        public void testForValidUserEmail() throws JsonProcessingException {
            //given:
            UserInfo userInfo = new UserInfo("subject", "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userInfo))
                    .addHeader("Content-Type", "application/json"));

            //and:
            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.generate();
            var jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            DecodedJWT token = mock(DecodedJWT.class);
            doReturn(jwt).when(token).getToken();
            doReturn(token).when(jwtVerifier).verify(jwtAuthentication.getToken());
            Account account = mock(Account.class);
            doReturn(account).when(accountRepository).findByProviderReference("sub");

            //when:
            Authentication auth = authenticationProvider.authenticate(jwtAuthentication);

            //then:
            assertThat(auth).isNotNull();
        }

        @Test
        @DisplayName("verification failed")
        public void testForFailedVerification() throws JsonProcessingException {
            //given:
            UserInfo userInfo = new UserInfo("subject", "name", "pref", "giv", "fam", "email@ebi.ac.uk");
            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userInfo))
                    .addHeader("Content-Type", "application/json"));

            //and: given a JWT Authentication
            JwtGenerator jwtGenerator = new JwtGenerator("elixir");
            String jwt = jwtGenerator.generateWithSubject("sub");
            var jwtAuthentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

            //and: JWT verifier will fail
            Exception verificationFailed = new JWTVerificationException("verification failed");
            doThrow(verificationFailed).when(jwtVerifier).verify(jwtAuthentication.getToken());


            //expect:
            assertThatThrownBy(() -> {
                authenticationProvider.authenticate(jwtAuthentication);
            }).isInstanceOf(JwtVerificationFailed.class);
        }

    }

}
