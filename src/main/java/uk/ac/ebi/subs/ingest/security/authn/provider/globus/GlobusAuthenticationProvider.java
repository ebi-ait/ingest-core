package uk.ac.ebi.subs.ingest.security.authn.provider.globus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.AccountRepository;
import uk.ac.ebi.subs.ingest.security.Role;
import uk.ac.ebi.subs.ingest.security.authn.oidc.OpenIdAuthentication;
import uk.ac.ebi.subs.ingest.security.authn.oidc.UserInfo;

@Component
@Qualifier("GLOBUS")
@Lazy
@Slf4j
public class GlobusAuthenticationProvider implements AuthenticationProvider {

    private final WebClient webClient;
    private final AccountRepository accountRepository;

    @Autowired
    public GlobusAuthenticationProvider(WebClient.Builder builder,
                                        AccountRepository accountRepository) {
        this.webClient = builder
                .baseUrl("https://auth.globus.org")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.accountRepository = accountRepository;

        log.info("[GlobusAuth] GlobusAuthenticationProvider initialised with baseUrl=https://auth.globus.org");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("[GlobusAuth] authenticate() entered with {}",
                authentication != null ? authentication.getClass().getName() : "null");

        if (!(authentication instanceof GlobusBearerAuthentication)) {
            log.trace("[GlobusAuth] Not a GlobusBearerAuthentication, delegating");
            return null;
        }

        GlobusBearerAuthentication globusAuth = (GlobusBearerAuthentication) authentication;
        String token = globusAuth.getToken();
        if (token == null || token.isBlank()) {
            log.debug("[GlobusAuth] Empty token, delegating");
            return null;
        }

        String tokenPreview = token.length() > 12 ? token.substring(0, 12) + "..." : token;
        log.debug("[GlobusAuth] Received token (preview): {}", tokenPreview);

        // Call Globus /userinfo with opaque access token
        UserInfo userInfo = fetchUserInfo(token);

        if (userInfo == null) {
            log.debug("[GlobusAuth] fetchUserInfo returned null – not a Globus token; delegating");
            return null;
        }

        log.debug("[GlobusAuth] UserInfo from Globus: sub={}, email={}, name={}, preferredUsername={}",
                userInfo.getSubjectId(),
                userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getPreferredUsername());

        if (userInfo.getSubjectId() == null) {
            log.warn("[GlobusAuth] UserInfo.subjectId is null – treating as invalid Globus userinfo");
            throw new AuthenticationServiceException("Invalid Globus user information (missing subjectId)");
        }

        // Resolve display name (email > name > preferredUsername > sub)
        String displayName = userInfo.getEmail();
        if (displayName == null || displayName.isBlank()) {
            displayName = userInfo.getName();
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = userInfo.getPreferredUsername();
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = userInfo.getSubjectId();
        }

        log.debug("[GlobusAuth] Resolved displayName='{}' for subjectId={}",
                displayName, userInfo.getSubjectId());

        // Load or create a *persisted* Account
        Account account = loadOrCreateAccount(userInfo, displayName);

        OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account);
        openIdAuth.authenticateWith(userInfo);

        log.info("[GlobusAuth] Successfully authenticated Globus user sub={}, displayName={}, accountId={}",
                userInfo.getSubjectId(), displayName, account.getId());

        return openIdAuth;
    }

    /**
     * Load an existing Account for this Globus subject, or create + save one.
     * providerReference is used to store the Globus "sub".
     */
    private Account loadOrCreateAccount(UserInfo userInfo, String displayName) {
        String globusSub = userInfo.getSubjectId();

        // 1) Try existing account by providerReference (exact sub match)
        Account existing = accountRepository.findByProviderReference(globusSub);
        if (existing != null) {
            // Optionally keep name in sync
            if (displayName != null && !displayName.isBlank()
                    && (existing.getName() == null || !displayName.equals(existing.getName()))) {
                existing.setName(displayName);
                existing = accountRepository.save(existing);
                log.debug("[GlobusAuth] Updated existing Account {} name to '{}'",
                        existing.getId(), displayName);
            } else {
                log.debug("[GlobusAuth] Reusing existing Account {} for sub={}",
                        existing.getId(), globusSub);
            }
            return existing;
        }

        // 2) No existing account → create new with default role GUEST (or whatever is minimal)
        Account account = new Account(globusSub); // providerReference = sub
        account.setName(displayName);
        account.addRole(Role.GUEST);  // 🔴 changed from Role.USER to Role.GUEST

        Account saved = accountRepository.save(account);
        log.debug("[GlobusAuth] Created new Account {} for Globus sub={}", saved.getId(), globusSub);

        return saved;
    }

    protected UserInfo fetchUserInfo(String accessToken) {
        String tokenPreview = accessToken == null ? "null" :
                (accessToken.length() > 12 ? accessToken.substring(0, 12) + "..." : accessToken);

        log.debug("[GlobusAuth] Calling /v2/oauth2/userinfo with access token (preview): {}", tokenPreview);

        try {
            UserInfo userInfo = webClient
                    .get()
                    .uri("/v2/oauth2/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();

            log.debug("[GlobusAuth] /v2/oauth2/userinfo returned: {}", userInfo);
            return userInfo;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.debug("[GlobusAuth] /userinfo rejected token with status {} – probably not a Globus token; delegating",
                        e.getStatusCode());
                return null;
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("[GlobusAuth] /v2/oauth2/userinfo returned 404 – check baseUrl and path", e);
            } else {
                log.error("[GlobusAuth] Error calling Globus /userinfo", e);
            }
            throw new AuthenticationServiceException("Error calling Globus /userinfo", e);
        } catch (Exception e) {
            log.error("[GlobusAuth] Unexpected error calling Globus /userinfo", e);
            throw new AuthenticationServiceException("Error calling Globus /userinfo", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean supported = GlobusBearerAuthentication.class.isAssignableFrom(authentication);
        log.trace("[GlobusAuth] supports({}) -> {}", authentication.getName(), supported);
        return supported;
    }
}
