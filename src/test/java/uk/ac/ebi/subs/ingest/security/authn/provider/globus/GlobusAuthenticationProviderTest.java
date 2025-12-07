package uk.ac.ebi.subs.ingest.security.authn.provider.globus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.function.client.WebClient;

import uk.ac.ebi.subs.ingest.security.Account;
import uk.ac.ebi.subs.ingest.security.AccountRepository;
import uk.ac.ebi.subs.ingest.security.Role;
import uk.ac.ebi.subs.ingest.security.authn.oidc.UserInfo;

public class GlobusAuthenticationProviderTest {

    /**
     * Test subclass that bypasses the real HTTP call and just returns a fixed UserInfo.
     */
    static class TestGlobusAuthenticationProvider extends GlobusAuthenticationProvider {

        private final UserInfo userInfoToReturn;

        TestGlobusAuthenticationProvider(UserInfo userInfoToReturn,
                                         AccountRepository accountRepository) {
            super(WebClient.builder(), accountRepository);
            this.userInfoToReturn = userInfoToReturn;
        }

        @Override
        protected UserInfo fetchUserInfo(String accessToken) {
            return userInfoToReturn;
        }
    }

    @Nested
    @DisplayName("authentication")
    class AuthenticationTest {

        @Test
        @DisplayName("authentication succeeds with valid Globus token + userinfo")
        void testAuthenticateSuccess() {
            // opaque token – we don't care about structure now
            String token = "opaque-globus-token-123";
            GlobusBearerAuthentication globusAuth = new GlobusBearerAuthentication(token);

            // Simulated userinfo from Globus
            UserInfo userInfo = new UserInfo("globus-sub-123", "Globus User");

            // Mock AccountRepository
            AccountRepository repo = mock(AccountRepository.class);

            // No existing account for this providerReference
            when(repo.findByProviderReference("globus-sub-123")).thenReturn(null);

            // Echo back whatever Account is saved
            when(repo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            GlobusAuthenticationProvider provider =
                    new TestGlobusAuthenticationProvider(userInfo, repo);

            Authentication result = provider.authenticate(globusAuth);

            assertThat(result).isNotNull();
            assertThat(result.getPrincipal()).isInstanceOf(Account.class);

            Account account = (Account) result.getPrincipal();

            // displayName resolves to name because email == null
            assertThat(account.getName()).isEqualTo("Globus User");
            assertThat(account.getProviderReference()).isEqualTo("globus-sub-123");
            // Use the role you actually add in loadOrCreateAccount (USER in the latest code)
            assertThat(account.getRoles()).contains(Role.GUEST);
        }

        @Test
        @DisplayName("authentication fails when UserInfo has no subjectId")
        void testAuthenticateNoSubject() {
            String token = "opaque-globus-token-456";
            GlobusBearerAuthentication globusAuth = new GlobusBearerAuthentication(token);

            // subjectId = null
            UserInfo userInfo = new UserInfo();

            AccountRepository repo = mock(AccountRepository.class);

            GlobusAuthenticationProvider provider =
                    new TestGlobusAuthenticationProvider(userInfo, repo);

            assertThatThrownBy(() -> provider.authenticate(globusAuth))
                    .isInstanceOf(AuthenticationServiceException.class)
                    .hasMessageContaining("Invalid Globus user information");
        }
    }
}
