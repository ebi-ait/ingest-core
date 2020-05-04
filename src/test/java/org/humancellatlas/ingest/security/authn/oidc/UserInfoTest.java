package org.humancellatlas.ingest.security.authn.oidc;

import org.humancellatlas.ingest.security.Account;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserInfoTest {

    @Test
    public void convertToAccount() {
        //given:
        String subjectId = "723b4001";
        String name = "Jean Valjean";
        UserInfo userInfo = new UserInfo(subjectId, "https://domain.tld/issuer", name);

        //when:
        Account account = userInfo.toAccount();

        //then:
        assertThat(account)
                .extracting("providerReference", "name")
                .containsExactly(subjectId, name);
    }

}
