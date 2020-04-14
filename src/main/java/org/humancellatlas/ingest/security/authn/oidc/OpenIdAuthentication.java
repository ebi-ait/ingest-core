package org.humancellatlas.ingest.security.authn.oidc;

import org.humancellatlas.ingest.security.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OpenIdAuthentication implements Authentication {

    private Account account;
    private UserInfo userInfo;

    private boolean authenticated = false;

    public OpenIdAuthentication(Account principal) {
        this(principal, null);
    }

    public OpenIdAuthentication(Account principal, UserInfo credentials) {
        this.account = principal;
        this.userInfo = credentials;
    }

    @Override
    public Object getPrincipal() {
        return account;
    }

    @Override
    public Object getCredentials() {
        return userInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.getRoles();
    }

    @Override
    public String getName() {
        return account.getProviderReference();
    }

    @Override
    public Object getDetails() {
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    public void authenticateWith(UserInfo credentials) {
        this.userInfo = credentials;
        if (account == null || credentials == null) {
            authenticated = false;
            return;
        }
        authenticated = credentials.getSubjectId().equalsIgnoreCase(account.getProviderReference());
    }

}
