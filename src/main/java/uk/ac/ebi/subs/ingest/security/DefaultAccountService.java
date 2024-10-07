package uk.ac.ebi.subs.ingest.security;

import org.springframework.stereotype.Component;

import uk.ac.ebi.subs.ingest.security.exception.DuplicateAccount;

@Component
public class DefaultAccountService implements AccountService {

  private final AccountRepository accountRepository;

  public DefaultAccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account register(Account account) {
    account.addRole(Role.CONTRIBUTOR);
    Account persistentAccount =
        accountRepository.findByProviderReference(account.getProviderReference());
    if (persistentAccount != null) {
      throw new DuplicateAccount();
    }
    return this.accountRepository.save(account);
  }
}
