package org.humancellatlas.ingest.security;

import org.humancellatlas.ingest.security.exception.DuplicateAccount;
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
        Account persistentAccount = accountRepository.findByProviderReference(account.getProviderReference());
        if (persistentAccount != null) {
            throw new DuplicateAccount();
        }
        return this.accountRepository.save(account);
    }

}
