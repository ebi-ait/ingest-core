package org.humancellatlas.ingest.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAuditingTest {
    @Test
    public void testGetCurrentAuditor() {
        // given
        Account userAccount = new Account("1", "elixir-1");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userAccount);

        UserAuditing userAuditing = new UserAuditing();

        // when
        Optional<String> userId = userAuditing.getCurrentAuditor();

        // then
        assertThat(userId.get()).isEqualTo("1");
    }

}
