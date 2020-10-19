package org.humancellatlas.ingest.security.authn.provider.elixir;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountRepository;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.common.jwk.DelegatingJwtAuthentication;
import org.humancellatlas.ingest.security.common.jwk.JwtVerifierResolver;
import org.humancellatlas.ingest.security.exception.JwtVerificationFailed;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;

@Component
@Qualifier(ELIXIR)
public class ElixirAaiAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElixirAaiAuthenticationProvider.class);

    private final JwtVerifierResolver jwtVerifierResolver;

    private final AccountRepository accountRepository;

    private final WebClient webClient;

    public ElixirAaiAuthenticationProvider(@Qualifier(ELIXIR) JwtVerifierResolver jwtVerifierResolver,
            AccountRepository accountRepository, WebClient.Builder webCliBuilder) {
        this.jwtVerifierResolver = jwtVerifierResolver;
        this.accountRepository = accountRepository;
        webClient = webCliBuilder.build();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        try {
            JwtAuthentication jwt = (JwtAuthentication) authentication;
            String token = jwt.getToken();
            String issuer = JWT.decode(token).getIssuer();
            verifyIssuer(issuer);

            JWTVerifier jwtVerifier = jwtVerifierResolver.resolve(jwt.getToken());
            DelegatingJwtAuthentication verifiedAuth = DelegatingJwtAuthentication.delegate(jwt, jwtVerifier);

            token = verifiedAuth.getToken();
            UserInfo userInfo = retrieveUserInfo(token);

            Account account = accountRepository.findByProviderReference(userInfo.getSubjectId());
            OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account);
            openIdAuth.authenticateWith(userInfo);
            return openIdAuth;
        } catch (TokenExpiredException e) {
            throw new JwtVerificationFailed(e);
        } catch (JWTVerificationException e) {
            LOGGER.error("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationFailed(e);
        }
    }

    private UserInfo retrieveUserInfo(String token) {
        return webClient.get()
                .uri(String.format("%s/userinfo", jwtVerifierResolver.getIssuer()))
                .header(AUTHORIZATION, String.format("Bearer %s", token))
                .retrieve()
                .bodyToMono(UserInfo.class).block();
    }

    private void verifyIssuer(String issuer) {
        if (!issuer.contains("elixir")) {
            throw new UnlistedJwtIssuer(String.format("Not an Elxir AAI issued token: %s", issuer), issuer);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}