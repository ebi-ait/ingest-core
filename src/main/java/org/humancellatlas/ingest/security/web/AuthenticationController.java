package org.humancellatlas.ingest.security.web;

import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountService;
import org.humancellatlas.ingest.security.Role;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.exception.DuplicateAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RequestMapping("/auth")
public class AuthenticationController {

    private final AccountService accountService;

    public AuthenticationController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(path="/registration", produces=APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> register(Authentication authentication) {
        var openIdAuthentication = (OpenIdAuthentication) authentication;
        var userInfo = (UserInfo) openIdAuthentication.getCredentials();
        try {
            Account account = new Account(userInfo.getSubjectId());
            Account persistentAccount = accountService.register(account);
            return ResponseEntity.ok().body(persistentAccount);
        } catch (DuplicateAccount duplicateAccount) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping(path="/account", produces=APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Account> getAccount(Authentication authentication) {
        if (authentication.getAuthorities().contains(Role.GUEST)) {
            return ResponseEntity.notFound().build();
        }
        Account account = (Account) authentication.getPrincipal();
        return ResponseEntity.ok().body(account);
    }

}
