package org.humancellatlas.ingest.security;

import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
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
import static org.springframework.http.HttpMethod.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    private static final List<AntPathRequestMatcher> SECURED_ANT_PATHS;
    static {
        List<AntPathRequestMatcher> antPathMatchers = new ArrayList<>();
        antPathMatchers.addAll(defineAntPathMatchers(GET, "/user/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PATCH, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(PUT, "/**"));
        antPathMatchers.addAll(defineAntPathMatchers(POST, "/messaging/**", "/projects**", "/submissionEnvelopes",
                "/submissionEnvelopes/*/projects", "/files**", "/biomaterials**", "/protocols**", "/processes**",
                "/files**", "/bundleManifests**"));
        SECURED_ANT_PATHS = Collections.unmodifiableList(antPathMatchers);
    }

    private static List<AntPathRequestMatcher> defineAntPathMatchers(HttpMethod method,
            String...patterns) {
        return Stream.of(patterns)
                .map(pattern -> new AntPathRequestMatcher(pattern, method.name()))
                .collect(toList());
    }

    private final AuthenticationProvider gcpleAuthenticationProvider;
    private final AuthenticationProvider elixirAuthenticationPovider;

    public SecurityConfig(@Qualifier(GCP) AuthenticationProvider gcp,
            @Qualifier(ELIXIR) AuthenticationProvider elixir) {
        this.gcpleAuthenticationProvider = gcp;
        this.elixirAuthenticationPovider = elixir;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authenticationProvider(elixirAuthenticationPovider)
                .authenticationProvider(gcpleAuthenticationProvider)
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
                .antMatchers(POST, "/auth/registration").hasAuthority(GUEST.name())
                .requestMatchers(this::isRequestForSecuredResourceFromProxy).authenticated()
                .antMatchers(GET, "/**").permitAll();
    }

    private Boolean isRequestForSecuredResourceFromProxy(HttpServletRequest request) {
        return SECURED_ANT_PATHS.stream().anyMatch(matcher -> matcher.matches(request)) &&
                Optional.ofNullable(request.getHeader(FORWARDED_FOR)).isPresent();
    }

}