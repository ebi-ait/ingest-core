package org.humancellatlas.ingest.security;


import org.humancellatlas.ingest.security.exception.DuplicateAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DefaultAccountService.class})
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        void success() {
            //given:
            String providerReference = "67fe90";
            Account account = new Account(providerReference);
            assumeThat(account.getRoles()).isEmpty();

            //and:
            String name = "Juan dela Cruz";
            account.setName(name);

            //and:
            Account persistentAccount = new Account("773b471", providerReference);
            persistentAccount.setName(name);
            doReturn(persistentAccount).when(accountRepository).save(any(Account.class));

            //when:
            Account result = accountService.register(account);

            //then:
            assertThat(result).isEqualTo(persistentAccount);
            var accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());

            //and:
            var savedAccount = accountCaptor.getValue();
            assertThat(savedAccount)
                    .extracting("providerReference", "name")
                    .containsExactly(providerReference, name);
            assertThat(savedAccount.getRoles()).containsOnly(Role.CONTRIBUTOR);
        }

        @Test
        void duplicateAccount() {
            //given:
            String providerReference = "84cd01b";
            Account account = new Account(providerReference);

            //and:
            Account persistentAccount = new Account("72b1c9e", providerReference);
            doReturn(persistentAccount).when(accountRepository).findByProviderReference(providerReference);

            //expect:
            assertThatThrownBy(() -> {
                accountService.register(account);
            }).isInstanceOf(DuplicateAccount.class);
        }
    }

}
