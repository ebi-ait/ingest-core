package org.humancellatlas.ingest.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.humancellatlas.ingest.security.jwk.RemoteJwkVault;
import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class RemoteServiceJwtVerifierResolverTest {

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
        RemoteServiceJwtVerifierResolver jwtVerifierResolver = new RemoteServiceJwtVerifierResolver(jwkVault, audience);

        //when:
        JWTVerifier verifier = jwtVerifierResolver.resolve(jwtGenerator.generate());

        //then:
        assertThat(verifier).isNotNull();

        //and: inspect using verifier with extended interface as a work around
        assertThat(verifier).isInstanceOf(DelegatingJwtVerifier.class);
        DelegatingJwtVerifier delegatingVerifier = (DelegatingJwtVerifier) verifier;
        assertThat(delegatingVerifier).extracting("audience").containsExactly(audience);
    }

}
