package org.humancellatlas.ingest.security;

import org.springframework.stereotype.Component;

@Component
public class DefaultAccountService implements AccountService {

    private final AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account register(Account account) {
        account.addRole(Role.CONTRIBUTOR);
        return this.accountRepository.save(account);
    }

}
