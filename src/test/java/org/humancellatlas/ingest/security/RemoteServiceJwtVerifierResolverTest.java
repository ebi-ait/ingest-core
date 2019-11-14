package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.humancellatlas.ingest.security.jwk.RemoteJwkVault;
import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GoogleServiceJwtVerifierResolverTest {

    @Test
    public void testResolveForJwt() {
        //given:
        JwtGenerator jwtGenerator = new JwtGenerator();
        RSAPublicKey publicKey = jwtGenerator.getPublicKey();

        //and:
        String audience = "https://dev.data.humancellatlas.org/";
        RemoteJwkVault jwkVault = mock(RemoteJwkVault.class);
        doReturn(publicKey).when(jwkVault).getPublicKey(any(DecodedJWT.class));

        //and:
        GoogleServiceJwtVerifierResolver jwtVerifierResolver = new GoogleServiceJwtVerifierResolver(jwkVault, audience);

        //when:
        JWTVerifier verifier = jwtVerifierResolver.resolve(jwtGenerator.generate());

        //then:
        assertThat(verifier).isNotNull();
    }

}
