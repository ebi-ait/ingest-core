package org.humancellatlas.ingest.security;


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
import static org.assertj.core.api.Assumptions.assumeThat;
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

            //when:
            accountService.register(account);

            //then:
            var accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());

            //and:
            var savedAccount = accountCaptor.getValue();
            assertThat(savedAccount.getProviderReference()).isEqualTo(providerReference);
            assertThat(savedAccount.getRoles()).contains(Role.CONTRIBUTOR);
        }

    }

}
