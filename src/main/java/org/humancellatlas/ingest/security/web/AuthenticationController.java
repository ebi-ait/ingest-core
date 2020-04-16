package org.humancellatlas.ingest.security.web;

import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountService;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public class AuthenticationController {

    private AccountService accountService;

    public AuthenticationController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/registration")
    public void register(Authentication authentication) {
        var openIdAuthentication = (OpenIdAuthentication) authentication;
        var userInfo = (UserInfo) openIdAuthentication.getCredentials();
        accountService.register(new Account(userInfo.getSubjectId()));
    }

}
