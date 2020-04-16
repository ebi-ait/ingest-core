package org.humancellatlas.ingest.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.security.Account;
import org.humancellatlas.ingest.security.AccountService;
import org.humancellatlas.ingest.security.SecurityConfig;
import org.humancellatlas.ingest.security.authn.oidc.OpenIdAuthentication;
import org.humancellatlas.ingest.security.authn.oidc.UserInfo;
import org.humancellatlas.ingest.security.exception.DuplicateAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.security.ElixirConfig.ELIXIR;
import static org.humancellatlas.ingest.security.GcpConfig.GCP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

        private static final String PATH = "/auth/registration";

        @Test
        void byAuthenticatedGuest() throws Exception {
            //given:
            String subjectId = "cf12881b";
            UserInfo userInfo = new UserInfo(subjectId, "https://oidc.domain.tld/auth");
            Authentication authentication = new OpenIdAuthentication(null, userInfo);

            //and:
            String accountId = "b4912b3";
            Account persistentAccount = new Account(accountId, subjectId);
            doReturn(persistentAccount).when(accountService).register(any(Account.class));

            //when:
            MvcResult result = webApp
                    .perform(post(PATH)
                            .with(authentication(authentication))
                            .with(csrf()))
                    .andReturn();

            //then:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            //and:
            ObjectMapper objectMapper = new ObjectMapper();
            var resultingAccount = objectMapper.readValue(response.getContentAsString(), Account.class);
            assertThat(resultingAccount.getId()).isEqualTo(accountId);

            //and:
            var accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountService).register(accountCaptor.capture());

            //and:
            var registeredAccount = accountCaptor.getValue();
            assertThat(registeredAccount)
                    .extracting("providerReference")
                    .containsExactly(userInfo.getSubjectId());
        }

        @Test
        @WithMockUser(roles={ "CONTRIBUTOR" })
        void byRegisteredUser() throws Exception {
            // expect:
            webApp.perform(post(PATH)).andExpect(status().isForbidden());
        }

        /*
        Similar scenario to byRegisteredUser but somehow the Account was either,
        1) unrecognised and so was treated as an authenticated Guest, or
        2) Account was erroneously assigned the Guest role.
        Essentially, we want to handle duplicated subject id in our system.
         */
        @Test
        void byUnrecognisedRegisteredUser() throws Exception {
            //given:
            UserInfo userInfo = new UserInfo("cc9a9a1", "https://secure.tld/auth");
            Authentication authentication = new OpenIdAuthentication(userInfo);

            //and:
            doThrow(new DuplicateAccount()).when(accountService).register(any(Account.class));

            //expect:
            webApp.perform(post(PATH)
                    .with(authentication(authentication))
                    .with(csrf()))
                    .andExpect(status().isConflict());
        }

        @Test
        void byAnonymousUser() throws Exception {
            // expect:
            webApp.perform(post(PATH)).andExpect(status().isUnauthorized());
        }

    }

}
