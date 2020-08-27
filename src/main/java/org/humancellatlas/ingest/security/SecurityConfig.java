package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.humancellatlas.ingest.security.Role.GUEST;
import static org.humancellatlas.ingest.security.Role.WRANGLER;
import static org.springframework.http.HttpMethod.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @NonNull
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    private static final List<AntPathRequestMatcher> SECURED_ANT_PATHS;
    static {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(POST, "/**"));
        SECURED_ANT_PATHS = Collections.unmodifiableList(antPathMatchers);
    }

    private static final List<AntPathRequestMatcher> SECURED_WRANGLER_ANT_PATHS;
    static {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/projects"));
        antPathMatchers.addAll(defineAntPathMatchers(PUT, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(DELETE, "/**"));
        SECURED_WRANGLER_ANT_PATHS = Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> defineAntPathMatchers(HttpMethod method,
            String...patterns) {
        return Stream.of(patterns)
                .map(pattern -> new AntPathRequestMatcher(pattern, method.name()))
                .collect(toList());
    }

    private final AuthenticationProvider gcpAuthenticationProvider;
    private final AuthenticationProvider elixirAuthenticationPovider;

    public SecurityConfig(@Qualifier(GCP) AuthenticationProvider gcp,
            @Qualifier(ELIXIR) AuthenticationProvider elixir) {
        this.gcpAuthenticationProvider = gcp;
        this.elixirAuthenticationPovider = elixir;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authenticationProvider(elixirAuthenticationPovider)
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
                .antMatchers(POST, "/submissionEnvelopes").authenticated()
                .antMatchers(POST, "/projects").authenticated()
                .antMatchers(GET, "/user/**").authenticated()
                .antMatchers(GET, "/auth/account").authenticated()
                .antMatchers(POST, "/auth/registration").hasAuthority(GUEST.name())
                .requestMatchers(this::isSecuredWranglerEndpointFromOutside).hasAuthority(WRANGLER.name())
                .requestMatchers(this::isSecuredEndpointFromOutside).authenticated()
                .antMatchers(GET, "/**").permitAll();
    }

    private Boolean isSecuredEndpointFromOutside(HttpServletRequest request) {
        return SECURED_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                this.isRequestOutsideProxy(request);
    }

    private Boolean isSecuredWranglerEndpointFromOutside(HttpServletRequest request) {
        return SECURED_WRANGLER_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                this.isRequestOutsideProxy(request);
    }

    private Boolean isRequestOutsideProxy(HttpServletRequest request) {
        this.log.debug("forwarded for header %s", request.getHeader(FORWARDED_FOR));
        return Optional.ofNullable(request.getHeader(FORWARDED_FOR)).isPresent();
    }



}