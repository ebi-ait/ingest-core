package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.humancellatlas.ingest.security.common.jwk.DelegatingJwtAuthentication;
import org.humancellatlas.ingest.security.common.jwk.RemoteServiceJwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.InvalidUserEmail;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.reactive.function.client.WebClient;

public class ElixirAaiAuthenticationProvider implements AuthenticationProvider {
    private static Logger logger = LoggerFactory.getLogger(ElixirAaiAuthenticationProvider.class);

    private final RemoteServiceJwtVerifierResolver jwtVerifierResolver;

    public ElixirAaiAuthenticationProvider(RemoteServiceJwtVerifierResolver jwtVerifierResolver) {
        this.jwtVerifierResolver = jwtVerifierResolver;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        try {
            JwtAuthentication jwt = (JwtAuthentication) authentication;
            verifyUserEmail(jwt);

            JWTVerifier jwtVerifier = jwtVerifierResolver.resolve(jwt.getToken());
            Authentication jwtAuth = DelegatingJwtAuthentication.delegate(jwt, jwtVerifier);
            logger.info("Authenticated with jwt with scopes {}", jwtAuth.getAuthorities());

            return jwtAuth;
        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationFailed(e);
        }
    }

    private void verifyUserEmail(JwtAuthentication jwt) {
        String token = jwt.getToken();
        String issuer = JWT.decode(token).getIssuer();

        if(! JWT.decode(jwt.getToken()).getIssuer().contains("elixir")) {
            throw new UnlistedJwtIssuer(String.format("Not an Elxir AAI issue: %s", issuer), issuer);
        }


        WebClient elixirClient = WebClient
                .builder()
                .baseUrl(jwtVerifierResolver.getIssuer())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        WebClient.RequestBodySpec userInfoRequest = elixirClient
                .method(HttpMethod.GET)
                .uri("/userinfo");
        ElixirUserInfo userInfo = userInfoRequest.retrieve().bodyToMono(ElixirUserInfo.class).block();

        if (userInfo.getEmail().indexOf("@ebi.ac.uk") < 0) {
            throw new InvalidUserEmail(userInfo.getEmail());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}