package org.humancellatlas.ingest.security;

import org.springframework.stereotype.Component;

@Component
public class DefaultAccountService implements AccountService {

    private final AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void register(Account account) {
        this.accountRepository.save(account);
    }

}
