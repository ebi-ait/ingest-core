package org.humancellatlas.ingest.security.web;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountService;
import org.humancellatlas.ingest.security.SecurityConfig;
import org.humancellatlas.ingest.security.web.test.IdToken;
import org.humancellatlas.ingest.security.web.test.IdTokenAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig({ AuthenticationController.class, SecurityConfig.class })
@AutoConfigureMockMvc(printOnlyOnFailure=false)
public class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private MockMvc webApp;

    @MockBean(name=GCP)
    private AuthenticationProvider gcp;

    @MockBean(name=ELIXIR)
    private AuthenticationProvider elixir;

    @MockBean
    private AccountService accountService;

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        void newUserWithValidJwt() throws Exception {
            //given:
            IdToken idToken = new IdToken("cf12881b", "Juan dela Cruz");

            // expect:
            webApp.perform(post("/auth/registration")
                    .with(authentication(new IdTokenAuthentication(idToken)))
                    .with(csrf()))
                    .andExpect(status().isOk());

            //and:
            var accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountService).register(accountCaptor.capture());

            //and:
            var registeredAccount = accountCaptor.getValue();
            assertThat(registeredAccount)
                    .extracting("providerReference")
                    .containsExactly(idToken.getSubject());
        }

    }

}
