package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.humancellatlas.ingest.security.Role.*;
import static org.springframework.http.HttpMethod.*;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String FORWARDED_HOST = "x-forwarded-host";

    private static final List<AntPathRequestMatcher> SECURED_ANT_PATHS = setupSecuredAntPaths();

    private static final List<AntPathRequestMatcher> SECURED_WRANGLER_ANT_PATHS = setupWranglerAntPaths();

    // The following endpoints are only secured when accessed from the outside the cluster

    private static List<AntPathRequestMatcher> setupSecuredAntPaths() {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(POST, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/projects/*"));
        return Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> setupWranglerAntPaths() {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/bundleManifests"));
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/submissionManifests"));

        antPathMatchers.addAll(defineAntPathMatchers(GET, "/submissionEnvelopes"));
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/biomaterials"));
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/files"));
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/processes"));
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/protocols"));

        antPathMatchers.addAll(defineAntPathMatchers(GET, "/projects"));
        antPathMatchers.addAll(defineAntPathMatchers(PUT, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(DELETE, "/**"));
        return Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> defineAntPathMatchers(HttpMethod method,
                                                                     String... patterns) {
        return Stream.of(patterns)
                .map(pattern -> new AntPathRequestMatcher(pattern, method.name()))
                .collect(toList());
    }

    private final AuthenticationProvider gcpAuthenticationProvider;
    private final AuthenticationProvider elixirAuthenticationProvider;

    public SecurityConfig(@Qualifier(GCP) AuthenticationProvider gcp,
                          @Qualifier(ELIXIR) AuthenticationProvider elixir) {
        this.gcpAuthenticationProvider = gcp;
        this.elixirAuthenticationProvider = elixir;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authenticationProvider(elixirAuthenticationProvider)
                .authenticationProvider(gcpAuthenticationProvider)
                .securityContext().securityContextRepository(new BearerSecurityContextRepository())
                .and()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers(GET, "/").permitAll()
                .antMatchers(GET, "/schemas/**").permitAll()
                .antMatchers(GET, "/health").permitAll()
                .antMatchers(GET, "/info").permitAll()
                .antMatchers(GET, "/prometheus").permitAll()
                .antMatchers(GET, "/browser/**").permitAll()
                .antMatchers(POST, "/submissionEnvelopes").authenticated()
                .antMatchers(POST, "/submissionEnvelopes/**").authenticated()
                .antMatchers(POST, "/projects").authenticated()
                .antMatchers(POST, "/dataset").permitAll()
                .antMatchers(POST, "/projects/suggestion").permitAll()
                .antMatchers(POST, "/projects/catalogue").permitAll()
                .antMatchers(GET, "/user/**").authenticated()
                .antMatchers(GET, "/auth/account").authenticated()
                .antMatchers(POST, "/auth/registration").hasAuthority(GUEST.name())
                .requestMatchers(SecurityConfig::isSecuredEndpointFromOutside).authenticated()
                .requestMatchers(SecurityConfig::isSecuredWranglerEndpointFromOutside)
                .hasAnyAuthority(WRANGLER.name(), SERVICE.name())
                .antMatchers(GET, "/**").authenticated();
    }

    private static Boolean isSecuredEndpointFromOutside(HttpServletRequest request) {
        return SECURED_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                SecurityConfig.isRequestOutsideProxy(request);
    }

    private static Boolean isSecuredWranglerEndpointFromOutside(HttpServletRequest request) {
        return SECURED_WRANGLER_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                SecurityConfig.isRequestOutsideProxy(request);
    }

    private static Boolean isRequestOutsideProxy(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(FORWARDED_HOST)).isPresent();
    }

}
