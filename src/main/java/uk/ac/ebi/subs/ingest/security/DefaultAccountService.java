package uk.ac.ebi.subs.ingest.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.ingest.security.exception.DuplicateAccount;

@Component
@Slf4j
public class DefaultAccountService implements AccountService {

  private final AccountRepository accountRepository;

  public DefaultAccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public Account register(Account account) {
    log.debug(
            "[AccountService] register() called for providerRef={}, currentId={}",
            account.getProviderReference(),
            account.getId());

    // Only assign a default role if none is present
    if (account.getRoles() == null || account.getRoles().isEmpty()) {
      account.addRole(Role.GUEST);
    }

    Account persistentAccount =
            accountRepository.findByProviderReference(account.getProviderReference());
    if (persistentAccount != null) {
      log.warn(
              "[AccountService] Duplicate registration attempt for providerRef={}, existingId={}",
              account.getProviderReference(),
              persistentAccount.getId());
      throw new DuplicateAccount();
    }

    Account saved = this.accountRepository.save(account);
    log.info(
            "[AccountService] New account persisted: id={}, providerRef={}",
            saved.getId(),
            saved.getProviderReference());

    // Persist and return the saved entity (with non-null id)
    return saved;
  }
}
