package org.humancellatlas.ingest.security;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    @Nested
    class Guest {

        @Test
        void ensureNoNullFields() {
            //expect:
            assertThat(Account.GUEST).hasNoNullFieldsOrProperties();
        }

        @Test
        void ensureGuestRole() {
            //expect:
            assertThat(Account.GUEST.getRoles()).containsOnly(Role.GUEST);
        }

    }

}
