package uk.ac.ebi.subs.ingest.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.subs.ingest.security.ElixirConfig.ELIXIR;
import static uk.ac.ebi.subs.ingest.security.GcpConfig.GCP;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.AccountService;
import uk.ac.ebi.subs.ingest.security.Role;
import uk.ac.ebi.subs.ingest.security.authn.oidc.OpenIdAuthentication;
import uk.ac.ebi.subs.ingest.security.authn.oidc.UserInfo;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class AuthenticationControllerTest {

  private static final String BASE_PATH = "/auth";

  @Autowired private WebApplicationContext applicationContext;

  @Autowired private MockMvc webApp;

  @MockBean(name = GCP)
  private AuthenticationProvider gcp;

  @MockBean(name = ELIXIR)
  private AuthenticationProvider elixir;

  @MockBean(name = "COGNITO")
  private AuthenticationProvider cognito;

  @MockBean(name = "GLOBUS")
  private AuthenticationProvider globusAuthenticationProvider;

  @MockBean private AccountService accountService;

  @Nested
  @DisplayName("Registration")
  class Registration {

    private static final String PATH = "/auth/registration";

    @Test
    void byAuthenticatedGuest() throws Exception {
      // given:
      String subjectId = "cf12881b";
      String accountId = "b4912b3";

      UserInfo userInfo = new UserInfo(subjectId, "Jane Doe");

      // Mimic what the real provider would do:
      Account account = new Account(accountId, subjectId);
      account.setName(userInfo.getName());
      account.addRole(Role.GUEST);

      Authentication authentication = new OpenIdAuthentication(account, userInfo);

      // when:
      MvcResult result =
          webApp.perform(post(PATH).with(authentication(authentication)).with(csrf())).andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

      ObjectMapper objectMapper = new ObjectMapper();
      var resultingAccount = objectMapper.readValue(response.getContentAsString(), Account.class);

      assertThat(resultingAccount.getId()).isEqualTo(accountId);
      assertThat(resultingAccount.getProviderReference()).isEqualTo(subjectId);
      assertThat(resultingAccount.getName()).isEqualTo("Jane Doe");
      assertThat(resultingAccount.getRoles()).containsExactly(Role.GUEST);

      // With the new controller, accountService should not be touched:
      verify(accountService, never()).register(any());
    }

    @Test
    @WithMockUser(roles = {"CONTRIBUTOR"})
    void byRegisteredUser() throws Exception {
      // A non-Account principal (e.g. default @WithMockUser) should be rejected
      webApp.perform(post(PATH).with(csrf())).andExpect(status().isForbidden());
    }

    /*
    Previously this scenario asserted a 409 when AccountService.register threw DuplicateAccount.
    Now /auth/registration is idempotent and simply returns the current Account principal.
    Any "duplicate subject id" handling belongs in the AuthenticationProvider, not this controller.
    We just verify that the endpoint still returns the given Account and does not call AccountService.
    */
    @Test
    void byUnrecognisedRegisteredUser() throws Exception {
      // given: some existing-looking guest account
      String subjectId = "cc9a9a1";
      String accountId = "existing-id";

      UserInfo userInfo = new UserInfo(subjectId, "");
      Account account = new Account(accountId, subjectId);
      account.addRole(Role.GUEST);

      Authentication authentication = new OpenIdAuthentication(account, userInfo);

      // when:
      MvcResult result =
          webApp
              .perform(post(PATH).with(authentication(authentication)).with(csrf()))
              .andExpect(status().isOk())
              .andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      ObjectMapper objectMapper = new ObjectMapper();
      Account resultingAccount =
          objectMapper.readValue(response.getContentAsString(), Account.class);

      assertThat(resultingAccount.getId()).isEqualTo(accountId);
      assertThat(resultingAccount.getProviderReference()).isEqualTo(subjectId);
      assertThat(resultingAccount.getRoles()).containsExactly(Role.GUEST);

      // No DuplicateAccount path anymore – controller doesn't call the service
      verify(accountService, never()).register(any());
    }

    @Test
    void byAnonymousUser() throws Exception {
      // Security config still requires authentication, so this stays 401
      webApp.perform(post(PATH)).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Account Retrieval")
  class AccountRetrieval {

    private final String PATH = String.format("%s/account", BASE_PATH);

    @Test
    void registeredUser() throws Exception {
      // given:
      String accountId = "bcdde10";
      String subjectId = "67135cc";

      // and:
      Account account = new Account(accountId, subjectId);
      account.addRole(Role.CONTRIBUTOR);

      // and:
      UserInfo credentials = new UserInfo(subjectId, "");
      Authentication authentication = new OpenIdAuthentication(account, credentials);

      // when:
      MvcResult result = webApp.perform(get(PATH).with(authentication(authentication))).andReturn();

      // then:
      MockHttpServletResponse response = result.getResponse();
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
      assertCorrectAccountDetails(response, accountId, subjectId);
    }

    private void assertCorrectAccountDetails(
        MockHttpServletResponse response, String accountId, String subjectId) throws Exception {
      ObjectMapper objectMapper = new ObjectMapper();
      Account retrievedAccount =
          objectMapper.readValue(response.getContentAsString(), Account.class);
      assertThat(retrievedAccount)
          .extracting("id", "providerReference")
          .containsExactly(accountId, subjectId);
      assertThat(retrievedAccount.getRoles()).containsExactly(Role.CONTRIBUTOR);
    }

    @Test
    void authenticatedGuest() throws Exception {
      // given:
      UserInfo userInfo = new UserInfo("82ffab9", "");
      Authentication authentication = new OpenIdAuthentication(userInfo);

      // expect:
      webApp
          .perform(get(PATH).with(authentication(authentication)))
          .andExpect(status().isNotFound());
    }

    @Test
    void unknownGuest() throws Exception {
      // expect:
      webApp.perform(get(PATH)).andExpect(status().isUnauthorized());
    }
  }
}
