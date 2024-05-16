package org.humancellatlas.ingest.security.authn.provider.aws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.spring.security.api.authentication.JwtAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.exception.UnlistedJwtIssuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;

@Component
@Qualifier("COGNITO")
@Lazy
@Slf4j
public class AwsCognitoServiceAuthenticationProvider implements AuthenticationProvider {
    private final Environment environment;
    private static final String AWS_COGNITO_NON_OPENID_SCOPE = "aws.cognito.signin.user.admin";
    private final WebClient webClient;
    public final String awsCognitoDomainUrl;

    @Autowired
    public AwsCognitoServiceAuthenticationProvider(final Environment environment, final WebClient.Builder webClientBuilder) {
        this.environment = environment;
        this.awsCognitoDomainUrl = this.environment.getProperty("AWS_COGNITO_DOMAIN");
        this.webClient = webClientBuilder.baseUrl(this.awsCognitoDomainUrl)
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
        final DecodedJWT decodedJWT = JWT.decode(accessToken);
        final String issuer = decodedJWT.getIssuer();
        final String scope = decodedJWT.getClaim("scope").asString();

        verifyIssuer(issuer);

        if (scope == null) {
            throw new AuthenticationServiceException("Invalid user information");
        }

        if (scope.equalsIgnoreCase(AWS_COGNITO_NON_OPENID_SCOPE)) {
            return authenticateWithNonOpenIdScope(decodedJWT);
        }

        return authenticateWithUserInfoEndpoint(accessToken);
    }

    private Authentication authenticateWithNonOpenIdScope(final DecodedJWT decodedJWT) {
        final String userName = decodedJWT.getClaim("username").asString();
        final String sub = decodedJWT.getClaim("sub").asString();
        final Date expiresAt = decodedJWT.getExpiresAt();

        // Check if the token is expired
        if (expiresAt != null && expiresAt.before(new Date())) {
            throw new AuthenticationServiceException("Token is expired");
        }

        if (userName == null || sub == null) {
            throw new AuthenticationServiceException("Invalid user information");
        }

        final UserInfo userInfo = new UserInfo(sub, userName);
        final Account account = userInfo.toAccount();

        account.setName(userInfo.getName());

        final OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account);
        openIdAuth.authenticateWith(userInfo);

        return openIdAuth;
    }

    private Authentication authenticateWithUserInfoEndpoint(final String accessToken) {
        try {
            final String userInfoUrl = awsCognitoDomainUrl + "/userinfo";
            final UserInfo userInfo = webClient.get()
                    .uri(userInfoUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();

            if (userInfo != null && userInfo.getEmail() != null) {
                final Account account = userInfo.toAccount();
                account.setName(userInfo.getEmail());

                final OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account);
                openIdAuth.authenticateWith(userInfo);

                return openIdAuth;
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
