package org.humancellatlas.ingest.security.authn.provider.aws;

import com.auth0.jwt.JWT;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Qualifier("COGNITO")
@Slf4j
public class AwsCognitoServiceAuthenticationProvider implements AuthenticationProvider {
    private final WebClient webClient;
    @Value("${AWS_COGNITO_DOMAIN}")
    public String awsCognitoDomainUrl;

    @Autowired
    public AwsCognitoServiceAuthenticationProvider(final WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(awsCognitoDomainUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        final JwtAuthentication jwt = (JwtAuthentication) authentication;
        final String accessToken = jwt.getToken();
        final String issuer = JWT.decode(accessToken).getIssuer();

        verifyIssuer(issuer);

        try {
            // Make a request to Cognito's user info endpoint to retrieve user information
            final UserInfo userInfo = webClient.get()
                    .uri("/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();

            // Validate user information as needed
            if (userInfo != null && userInfo.getEmail() != null) {
                return new OpenIdAuthentication(userInfo.toAccount());
            } else {
                throw new AuthenticationServiceException("Invalid user information");
            }
        } catch (final Exception e) {
            throw new AuthenticationServiceException("Error authenticating with Cognito", e);
        }
    }

    private void verifyIssuer(final String issuer) {
        if (!issuer.contains("cognito")) {
            throw new UnlistedJwtIssuer(String.format("Not a Cognito issued token: %s", issuer), issuer);
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
