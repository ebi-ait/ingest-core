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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;

@Component
@Qualifier(ELIXIR)
public class ElixirAaiAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElixirAaiAuthenticationProvider.class);

    private final JwtVerifierResolver jwtVerifierResolver;

    private final AccountRepository accountRepository;

    private final WebClient webClient;

    // Counters for periodic logging
    private static final AtomicInteger userInfoRequestCounter = new AtomicInteger(0);
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static final AtomicInteger cacheHits = new AtomicInteger(0);
    private static final AtomicInteger cacheMisses = new AtomicInteger(0);

    // Cache for UserInfo with timestamps
    private final Map<String, UserInfo> userInfoCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long DEFAULT_CACHE_TTL = 60000; // Default TTL: 60 seconds

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
            LOGGER.info("Authentication attempt started for token: {}", truncateToken(token));

            String issuer = JWT.decode(token).getIssuer();
            verifyIssuer(issuer);

            LOGGER.info("Issuer verified: {}", issuer);

            JWTVerifier jwtVerifier = jwtVerifierResolver.resolve(jwt.getToken());
            DelegatingJwtAuthentication verifiedAuth = DelegatingJwtAuthentication.delegate(jwt, jwtVerifier);

            token = verifiedAuth.getToken();
            UserInfo userInfo = retrieveUserInfo(token);

            LOGGER.info("UserInfo retrieved successfully for subject ID: {}", userInfo.getSubjectId());

            Account account = accountRepository.findByProviderReference(userInfo.getSubjectId());
            OpenIdAuthentication openIdAuth = new OpenIdAuthentication(account);
            openIdAuth.authenticateWith(userInfo);

            LOGGER.info("Authentication succeeded for subject ID: {}", userInfo.getSubjectId());
            successCount.incrementAndGet();

            return openIdAuth;
        } catch (TokenExpiredException e) {
            LOGGER.error("Token expired: {}", e.getMessage());
            failureCount.incrementAndGet();
            throw new JwtVerificationFailed(e);
        } catch (JWTVerificationException e) {
            LOGGER.error("JWT verification failed: {}", e.getMessage());
            failureCount.incrementAndGet();
            throw new JwtVerificationFailed(e);
        }
    }

    private UserInfo retrieveUserInfo(String token) {
        long startTime = System.currentTimeMillis();
        userInfoRequestCounter.incrementAndGet();

        // Check if the UserInfo is in cache and still valid
        if (userInfoCache.containsKey(token)) {
            Long timestamp = cacheTimestamps.get(token);
            if (timestamp != null && isCacheValid(token, timestamp)) {
                LOGGER.info("Fetched UserInfo from cache for token: {}", truncateToken(token));
                cacheHits.incrementAndGet();
                return userInfoCache.get(token);
            } else {
                // Remove expired entry from cache
                userInfoCache.remove(token);
                cacheTimestamps.remove(token);
                LOGGER.info("Cache expired for token: {}", truncateToken(token));
                cacheMisses.incrementAndGet();
            }
        } else {
            cacheMisses.incrementAndGet();
        }

        // Fetch from LS if not in cache or expired
        try {
            UserInfo userInfo = webClient.get()
                    .uri(String.format("%s/userinfo", jwtVerifierResolver.getIssuer()))
                    .header(AUTHORIZATION, String.format("Bearer %s", token))
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();

            long elapsedTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Successfully fetched UserInfo in {} ms for token: {}", elapsedTime, truncateToken(token));

            // Update cache with dynamic TTL based on token expiry
            long expiryTime = getTokenExpiry(token);
            cacheTimestamps.put(token, System.currentTimeMillis());
            userInfoCache.put(token, userInfo);

            LOGGER.info("Updated cache for token: {}, Cache expiry in {} ms", truncateToken(token), expiryTime - System.currentTimeMillis());

            return userInfo;
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Failed to fetch UserInfo in {} ms for token: {}", elapsedTime, truncateToken(token), e);
            throw e;
        }
    }

    private boolean isCacheValid(String token, Long timestamp) {
        long expiryTime = getTokenExpiry(token);
        return (System.currentTimeMillis() - timestamp) < Math.min(expiryTime - System.currentTimeMillis(), DEFAULT_CACHE_TTL);
    }

    private long getTokenExpiry(String token) {
        try {
            return JWT.decode(token).getExpiresAt().getTime();
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve token expiry. Using default cache TTL.", e);
            return System.currentTimeMillis() + DEFAULT_CACHE_TTL;
        }
    }

    private void verifyIssuer(String issuer) {
        LOGGER.info("Verifying issuer: {}", issuer);
        if (!issuer.contains("elixir")) {
            LOGGER.error("Unlisted issuer: {}", issuer);
            throw new UnlistedJwtIssuer(String.format("Not an Elixir AAI issued token: %s", issuer), issuer);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }

    private String truncateToken(String token) {
        return token.substring(0, Math.min(20, token.length()));
    }

    @Scheduled(fixedRate = 60000)
    public void logPeriodicSummary() {
        int userInfoRequests = userInfoRequestCounter.getAndSet(0);
        int successes = successCount.getAndSet(0);
        int failures = failureCount.getAndSet(0);
        int hits = cacheHits.getAndSet(0);
        int misses = cacheMisses.getAndSet(0);
        int totalCacheAccesses = hits + misses;

        if (userInfoRequests > 0) {
            double successRate = calculateRate(successes, userInfoRequests);
            double cacheHitRate = calculateRate(hits, totalCacheAccesses);

            LOGGER.info("Periodic Summary (Last Minute):");
            LOGGER.info("- Total UserInfo Requests: {}", userInfoRequests);
            LOGGER.info("- Successful Requests: {}", successes);
            LOGGER.info("- Failed Requests: {}", failures);
            LOGGER.info("- Success Rate: {}%", String.format("%.2f", successRate));
            LOGGER.info("- Cache Hits: {}", hits);
            LOGGER.info("- Cache Misses: {}", misses);
            LOGGER.info("- Cache Hit Rate: {}%", String.format("%.2f", cacheHitRate));
        } else {
            LOGGER.info("No UserInfo requests were made in the last minute.");
        }
    }

    private double calculateRate(int part, int total) {
        return (total > 0) ? (part * 100.0 / total) : 0.0;
    }
}
