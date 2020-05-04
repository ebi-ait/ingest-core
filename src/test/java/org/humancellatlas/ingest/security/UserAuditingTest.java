package org.humancellatlas.ingest.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        void forOpenIdAuthentication() {
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

        @Test
        void forOtherAuthentication() {
            //given:
            String principal = "jdelacruz";
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "pas$w0rd",
                    asList(Role.CONTRIBUTOR));
            SecurityContextImpl securityContext = new SecurityContextImpl(authentication);
            SecurityContextHolder.setContext(securityContext);

            //when:
            String auditor = userAuditing.getCurrentAuditor().orElseThrow();

            //then:
            assertThat(auditor).isEqualTo(principal);
        }

    }

}
