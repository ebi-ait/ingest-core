package uk.ac.ebi.subs.ingest.security.authn.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import uk.ac.ebi.subs.ingest.security.Account;

public class UserInfoTest {

  @Test
  public void convertToAccount() {
    // given:
    String subjectId = "723b4001";
    String name = "Jean Valjean";
    UserInfo userInfo = new UserInfo(subjectId, name);

    // when:
    Account account = userInfo.toAccount();

    // then:
    assertThat(account).extracting("providerReference", "name").containsExactly(subjectId, name);
  }
}
