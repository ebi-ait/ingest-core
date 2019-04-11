package org.humancellatlas.ingest.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.BearerSecurityContextRepository;
import com.auth0.spring.security.api.JwtAuthenticationEntryPoint;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value(value = "${USR_AUTH_AUDIENCE:https://dev.data.humancellatlas.org/}")
    private String audience;
    @Value(value = "${AUTH_ISSUER:https://humancellatlas.auth0.com/}")
    private String issuer;

    @Value(value = "${SVC_AUTH_AUDIENCE:https://dev.data.humancellatlas.org/}")
    private String serviceAudience;

    @Value(value= "${GCP_PROJECT_WHITELIST:hca-dcp-production.iam.gserviceaccount.com,human-cell-atlas-travis-test.iam.gserviceaccount.com,broad-dsde-mint-dev.iam.gserviceaccount.com,broad-dsde-mint-test.iam.gserviceaccount.com,broad-dsde-mint-staging.iam.gserviceaccount.com}")
    private String projectWhitelist;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        List<String> projectWhitelist = Arrays.asList(this.projectWhitelist.split(","));
        GoogleServiceJwtAuthenticationProvider googleServiceJwtAuthenticationProvider = new GoogleServiceJwtAuthenticationProvider(serviceAudience, projectWhitelist);

        JwkProvider jwkProvider = new JwkProviderBuilder(issuer).build();
        JwtAuthenticationProvider auth0Provider = new JwtAuthenticationProvider(jwkProvider, issuer, audience);

        // FIXME: This is temporary workaround to be able to also verify tokens created from Dan Vaughan's Auth0 account
        String issuer2 = "https://danielvaughan.eu.auth0.com/";
        String audience2 = "http://localhost:8080";

        JwkProvider jwkProvider2 = new JwkProviderBuilder(issuer2).build();
        JwtAuthenticationProvider auth0Provider2 = new JwtAuthenticationProvider(jwkProvider2, issuer2, audience2);
        // FIXME: Remove 'til here

        http.authenticationProvider(auth0Provider)
                .authenticationProvider(googleServiceJwtAuthenticationProvider)
                .authenticationProvider(auth0Provider2) // FIXME: Remove soon
                .securityContext()
                .securityContextRepository(new BearerSecurityContextRepository())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .cors().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/user/**").authenticated()
                .antMatchers(HttpMethod.POST, "/submissionEnvelopes").authenticated()
                .antMatchers(HttpMethod.POST, "/messaging/**").authenticated()
                .antMatchers(HttpMethod.POST, "/projects").authenticated()
                .antMatchers(HttpMethod.POST, "/submissionEnvelopes/*/projects").authenticated()
                .antMatchers(HttpMethod.GET, "/**").permitAll();
    }
}
