package org.humancellatlas.ingest.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
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

    @Test
    public void testDecode() throws Exception {
        //given:
        var tokenFilePath = RESOURCE_PATH.resolve("jwt/access-token.jwt");
        assumeThat(tokenFilePath.toFile().exists()).isTrue();
        var jwtToken = Files.readString(tokenFilePath);

        //and:
        var decodedToken = JWT.decode(jwtToken);
        System.out.println(decodedToken.getToken());
    }

    @Test
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

    //TODO add checks for whitelisted issuers

}
