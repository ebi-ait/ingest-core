package org.humancellatlas.ingest.security;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//  TODO It would be better if we source the ff info from environment variables
    @Value(value = "${auth0.apiAudience}")
    private String apiAudience;
    @Value(value = "${auth0.issuer}")
    private String issuer;

    @Value(value = "${gservice.audience}")
    private String serviceAudience;
    @Value(value = "${gservice.project}")
    private String serviceProject;



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ServiceJwtAuthenticationProvider serviceJwtAuthenticationProvider = new ServiceJwtAuthenticationProvider(serviceAudience, serviceProject);

        // FIXME: This is temporary workaround to be able to also verify tokens created from Dan Vaughan's Auth0 account
        String issuer2 = "https://danielvaughan.eu.auth0.com/";
        String audience2 = "http://localhost:8080";
        JwkProvider jwkProvider = new JwkProviderBuilder(issuer2).build();
        JwtAuthenticationProvider auth0Provider = new JwtAuthenticationProvider(jwkProvider, issuer2, audience2);

        JwtWebSecurityConfigurer
                .forRS256(apiAudience, issuer)
                .configure(http)
                .authenticationProvider(serviceJwtAuthenticationProvider)
                .authenticationProvider(auth0Provider) // FIXME: Remove soon
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