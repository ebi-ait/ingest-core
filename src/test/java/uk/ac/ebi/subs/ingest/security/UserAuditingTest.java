package uk.ac.ebi.subs.ingest.security;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class UserAuditingTest {

  private UserAuditing userAuditing;

  @BeforeEach
  void setUp() {
    userAuditing = new UserAuditing();
  }

  @Nested
  @DisplayName("getCurrentAuditor")
  class GetCurrentAuditor {

    @Test
    void accountTypePrincipal() {
      // given:
      String providerReference = "6700ed52";
      Account userAccount = new Account(providerReference, "elixir-1");

      // and:
      Authentication authentication = mock(Authentication.class);
      doReturn(userAccount).when(authentication).getPrincipal();
      when(authentication.isAuthenticated()).thenReturn(true);

      // and:
      SecurityContext securityContext = new SecurityContextImpl(authentication);
      SecurityContextHolder.setContext(securityContext);

      // when:
      String auditor = userAuditing.getCurrentAuditor().orElseThrow();

      // then:
      assertThat(auditor).isEqualTo(providerReference);
    }

    @Test
    void nonAccountTypePrincipal() {
      // given:
      String principal = "jdelacruz";
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(principal, "pas$w0rd", asList(Role.CONTRIBUTOR));
      SecurityContextImpl securityContext = new SecurityContextImpl(authentication);
      SecurityContextHolder.setContext(securityContext);

      // when:
      String auditor = userAuditing.getCurrentAuditor().orElseThrow();

      // then:
      assertThat(auditor).isEqualTo(principal);
    }
  }
}
