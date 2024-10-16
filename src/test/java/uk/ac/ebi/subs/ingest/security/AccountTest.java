package uk.ac.ebi.subs.ingest.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AccountTest {

  @Nested
  class Guest {

    @Test
    void ensureNoNullFields() {
      // expect:
      assertThat(Account.GUEST).hasNoNullFieldsOrProperties();
    }

    @Test
    void ensureGuestRole() {
      // expect:
      assertThat(Account.GUEST.getRoles()).containsOnly(Role.GUEST);
    }
  }
}
