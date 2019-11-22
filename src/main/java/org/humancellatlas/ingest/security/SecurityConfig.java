package org.humancellatlas.ingest.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.humancellatlas.ingest.security.jwk.RemoteJwkVault;
import org.humancellatlas.ingest.security.jwk.UrlJwkProviderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration
        .WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    @Value(value = "${USR_AUTH_GROUP}")
    private String userGroup;

    @Value("${GCP_JWK_PROVIDER_BASE_URL}")
    private String googleJwkProviderbaseUrl;

    @Value(value = "${AUTH_ISSUER}")
    private String issuer;

    @Value(value = "${SVC_AUTH_AUDIENCE}")
    private String serviceAudience;

    @Value("${USR_AUTH_AUDIENCE}")
    private String audience;

    @Value(value= "#{('${GCP_PROJECT_WHITELIST}').split(',')}")
    private String[] projectWhitelist;

    private static final List<AntPathRequestMatcher> SECURED_ANT_PATHS;
    static {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/user/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PUT, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(POST,"/messaging/**", "/submissionEnvelopes/*/projects",
                "/files**", "/biomaterials**", "/protocols**", "/processes**", "/files**", "/bundleManifests**"));
        SECURED_ANT_PATHS = Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> defineAntPathMatchers(HttpMethod method,
            String...patterns) {
        return Stream.of(patterns)
                .map(pattern -> new AntPathRequestMatcher(pattern, method.name()))
                .collect(toList());
    }

    @Bean
    public AuthenticationProvider googleServiceAuthenticationProvider() {
        UrlJwkProviderResolver urlJwkProviderResolver = new UrlJwkProviderResolver(googleJwkProviderbaseUrl);
        RemoteJwkVault googleJwkVault = new RemoteJwkVault(urlJwkProviderResolver);
        RemoteServiceJwtVerifierResolver googleJwtVerifierResolver =
                new RemoteServiceJwtVerifierResolver(googleJwkVault, audience);
        return new GoogleServiceJwtAuthenticationProvider(new DomainWhiteList(projectWhitelist), googleJwtVerifierResolver);
    }

    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();
        JwtAuthenticationProvider delegate = new JwtAuthenticationProvider(jwkProvider, issuer, audience);
        return new UserJwtAuthenticationProvider(delegate);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authenticationProvider(userAuthenticationProvider())
                .authenticationProvider(googleServiceAuthenticationProvider())
                .securityContext().securityContextRepository(new BearerSecurityContextRepository())
                .and()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/submissionEnvelopes").authenticated()
                .antMatchers(HttpMethod.POST, "/projects**").authenticated()
                .requestMatchers(this::isRequestForSecuredResourceFromProxy).authenticated()
                .antMatchers(GET, "/**").permitAll();
    }

    private Boolean isRequestForSecuredResourceFromProxy(HttpServletRequest request) {
        return SECURED_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                Optional.ofNullable(request.getHeader(FORWARDED_FOR)).isPresent();
    }

}